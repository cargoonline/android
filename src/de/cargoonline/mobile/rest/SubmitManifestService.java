package de.cargoonline.mobile.rest;
  
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;  
import org.json.JSONArray;  
import de.cargoonline.mobile.MainMenuActivity;
import android.app.IntentService;  
import android.content.Intent;
import android.util.Log;

public class SubmitManifestService extends IntentService {

	public static final String TAG = "CO ManifestDataService";
	
	public final static String[] KEY_EORI_NO = {"eori_nr","nl_nr"};
	public final static String[] KEY_AWB_NO = {"mawb_prefix","mawb_nr"};
	public final static String KEY_MRN_NO = "mrn_nr";
	public final static String KEY_FLIGHT_NO = "extinf_befoerderm_kz";
	public final static String KEY_FLIGHT_LOCATION = "extinf_befoerderm_ladeort";
	public final static String KEY_STATUS = "b_status";
	public final static String KEY_SPEDITION_NAME = "spedition_name";
	public final static String KEY_POSITIONS = "positions";

	public SubmitManifestService() { 
		super("SubmitManifestService");
	} 

	@Override
	protected void onHandleIntent(Intent intent) {
		 
		ArrayList<String> positions = intent.getStringArrayListExtra(ManifestDataService.KEY_POSITIONS);
		String manifestId = intent.getStringExtra(WebExtClient.KEY_MANIFEST_ID);
		String speditionId = intent.getStringExtra(WebExtClient.KEY_SPEDITION_ID);
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(COServiceReceiver.ACTION_SUBMIT);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
				
		Log.d(TAG, "Now submitting MRNS.");
	    boolean result = submitMRNPositions(positions, speditionId, manifestId); 
		 
		if (result == false)
			broadcastIntent.putExtra(WebExtClient.KEY_NO_CONNECTION, true);
		else 
			broadcastIntent.putExtra(WebExtClient.KEY_RESPONSE, result);
		
		sendBroadcast(broadcastIntent);	 
	}
	
	
	public boolean submitMRNPositions(ArrayList<String> mrns, String speditionId, String manifestId) {
	
		if (mrns == null || mrns.size() == 0) return false; // no mrns to submit, continue.
	
		String regId = MainMenuActivity.prefs.getString(ServerUtilities.PROPERTY_REG_ID, "");
		String user = MainMenuActivity.prefs.getString(ServerUtilities.PROPERTY_USER_NAME, "");	 
		
		// let's build params and trigger post request		
		HashMap<String,String> params = new HashMap<String,String>();
			
		// if registered: send username & gcm reg id to enable push notifications
		if (!regId.equals("") && !user.equals("")) {
			params.put(ServerUtilities.PROPERTY_REG_ID, regId);
			params.put(ServerUtilities.PROPERTY_USER_NAME, user);
		}
		// in any case: send mrn numbers
		JSONArray positions = new JSONArray();
		for (String mrn : mrns) positions.put(mrn);				
		params.put("positions", positions.toString());
		
		// furthermore: spedition id ("zoll_nr"), manifest id, submission time and request key for service
		params.put(WebExtClient.KEY_SPEDITION_ID, speditionId);
		params.put(WebExtClient.KEY_MANIFEST_ID, manifestId);
		params.put(WebExtClient.KEY_REQUEST, WebExtClient.KEY_REQUEST_SUBMIT);
		params.put("time", Long.toString(System.currentTimeMillis()));
	
	    long backoff = ServerUtilities.BACKOFF_MILLI_SECONDS + ServerUtilities.random.nextInt(1000);
	    
		for (int i=0; i < ServerUtilities.MAX_ATTEMPTS; i++) { 
			
			try {
				int status = ServerUtilities.post(WebExtClient.getInstance(this).getMobileUserRestService(), params);
				if (status == 200) return true;
			} catch (IOException e) {
	            // should retry only on unrecoverable errors (like HTTP error code 503).
	            Log.e(TAG, "Failed to load manifest on attempt " + i + ":" + e);
	            if (i == ServerUtilities.MAX_ATTEMPTS) {
	                break;
	            }
	            try {
	                Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
	                Thread.sleep(backoff);
	            } catch (InterruptedException e1) {
	                // Activity finished before we complete - exit.
	                Log.d(TAG, "Thread interrupted: abort remaining retries!");
	                Thread.currentThread().interrupt();
	                return false;
	            }
	            // increase backoff exponentially
	            backoff *= 2;
	        } 			
		}
		return true;
	}
}
