package de.cargoonline.mobile.rest;
 
import java.io.IOException;
import java.util.HashMap;
import java.util.Map; 
import de.cargoonline.mobile.MainMenuActivity;
import android.app.IntentService; 
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ManifestDataService extends IntentService {

	public static final String TAG = "CO ManifestDataService";
	
	public final static String[] KEY_EORI_NO = {"eori_nr","nl_nr"};
	public final static String[] KEY_AWB_NO = {"mawb_prefix","mawb_nr"};
	public final static String KEY_MRN_NO = "mrn_nr";
	public final static String KEY_DETAIL_TEXT = "detail_txt";
	public final static String KEY_FLIGHT_NO = "extinf_befoerderm_kz";
	public final static String KEY_FLIGHT_LOCATION = "extinf_befoerderm_ladeort";
	public final static String KEY_STATUS = "b_status";
	public final static String KEY_SPEDITION_NAME = "spedition_name";
	public final static String KEY_POSITIONS = "positions";
	public final static String KEY_AWB_POSITION = "awb_position";
	
	private String manifestID;
	private String speditionID;
	private String manifestPwd;

	public ManifestDataService() { 
		super("ManifestDataService");
	} 
	  
	@Override
	protected void onHandleIntent(Intent intent) {
		SharedPreferences prefs = getSharedPreferences(MainMenuActivity.PREF_STORE, Context.MODE_PRIVATE);
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(intent.getStringExtra(WebExtClient.KEY_REQUEST));
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
				
		manifestID = prefs.getString(WebExtClient.KEY_MANIFEST_ID, "");
		speditionID = prefs.getString(WebExtClient.KEY_SPEDITION_ID, "");
		manifestPwd = prefs.getString(WebExtClient.KEY_MANIFEST_PWD, ""); 

		Log.d(TAG, "Now loading manifest data.");
	    String result = getManifestData(speditionID, manifestID, manifestPwd); 
		 
		if (result == null)
			broadcastIntent.putExtra(WebExtClient.KEY_NO_CONNECTION, true);
		else 
			broadcastIntent.putExtra(WebExtClient.KEY_RESPONSE, result);
		
		sendBroadcast(broadcastIntent);	 
	}

	public String getManifestData(String speditionId, String manifestId, String manifestPwd) throws IllegalArgumentException {
    	
		if (manifestId == null || manifestId.equals(""))
			throw new IllegalArgumentException("No valid manifest id!");
		if (speditionId == null || speditionId.equals(""))
			throw new IllegalArgumentException("No valid spedition id!");
		if (manifestPwd == null || manifestPwd.equals(""))
			throw new IllegalArgumentException("No valid manifest password!");
		
         
        Map<String,String> params = new HashMap<String,String>();
        params.put(WebExtClient.KEY_REQUEST, WebExtClient.KEY_REQUEST_SEARCH);
        params.put(WebExtClient.KEY_MANIFEST_ID, manifestId);
        params.put(WebExtClient.KEY_SPEDITION_ID, speditionId);
        params.put(WebExtClient.KEY_MANIFEST_PWD, manifestPwd);
         
        long backoff = ServerUtilities.BACKOFF_MILLI_SECONDS + ServerUtilities.random.nextInt(1000);
        
		for (int i=0; i < ServerUtilities.MAX_ATTEMPTS; i++) { 
			
			try {
				return ServerUtilities.get(WebExtClient.getInstance(this).getManifestRestService(), params);
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
	                return null;
	            }
	            // increase backoff exponentially
	            backoff *= 2;
	        } 
		} 
        return null;
    } 
}
