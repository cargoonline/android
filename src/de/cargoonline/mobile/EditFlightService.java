package de.cargoonline.mobile;

import java.io.IOException;
import java.util.HashMap;
import de.cargoonline.mobile.rest.COServiceReceiver;
import de.cargoonline.mobile.rest.ManifestDataService;
import de.cargoonline.mobile.rest.ServerUtilities;
import de.cargoonline.mobile.rest.WebExtClient;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


public class EditFlightService extends IntentService {

	public static final String TAG = "CO EditFlightService";

	private int awbPosition;
	
	public EditFlightService() {
		super("EditFlightService");
	}

	@Override
	protected void onHandleIntent(Intent i) {
		awbPosition = i.getIntExtra(ManifestDataService.KEY_AWB_POSITION, 0);
		String awbNo = i.getStringExtra(ManifestDataService.KEY_AWB_NO[1]);	
		String flightNo = i.getStringExtra(ManifestDataService.KEY_FLIGHT_NO);
		String flightLocation = i.getStringExtra(ManifestDataService.KEY_FLIGHT_LOCATION);
		String manifestId = i.getStringExtra(WebExtClient.KEY_MANIFEST_ID);
		String speditionId = i.getStringExtra(WebExtClient.KEY_SPEDITION_ID);
		 
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(COServiceReceiver.ACTION_EDIT_FLIGHT);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
				
		Log.d(TAG, "Now changing flight data...");
	    String error = editFlightData(flightNo, flightLocation, awbNo, manifestId, speditionId); 
		 
		if (error.equals(WebExtClient.KEY_INPUT_ERROR))
			broadcastIntent.putExtra(WebExtClient.KEY_RESPONSE, awbPosition);		
		else 
			broadcastIntent.putExtra(error, true);
		
		sendBroadcast(broadcastIntent);	
	}
	
	private String editFlightData(String flightNo, String flightLocation, String awbNo, String manifestId, String speditionId) {
		if (flightNo.equals("") || flightLocation.equals(""))
			return WebExtClient.KEY_INPUT_ERROR;		

		// let's build params and trigger post request		
		HashMap<String,String> params = new HashMap<String,String>();
		
		// send new flight data and awb no
		params.put(ManifestDataService.KEY_FLIGHT_NO, flightNo);
		params.put(ManifestDataService.KEY_FLIGHT_LOCATION, flightLocation);
		params.put(ManifestDataService.KEY_AWB_NO[1], awbNo);
				
		// also send username & regId (not required at the moment, but useful later to be able to track user activity...)
		String regId = MainMenuActivity.prefs.getString(ServerUtilities.PROPERTY_REG_ID, "");
		String user = MainMenuActivity.prefs.getString(ServerUtilities.PROPERTY_USER_NAME, "");	 
		if (!regId.equals("") && !user.equals("")) {
			params.put(ServerUtilities.PROPERTY_REG_ID, regId);
			params.put(ServerUtilities.PROPERTY_USER_NAME, user);
		}
		
		// furthermore: spedition id ("zoll_nr"), manifest id, submission time and request key for service
		params.put(WebExtClient.KEY_SPEDITION_ID, speditionId);
		params.put(WebExtClient.KEY_MANIFEST_ID, manifestId);
		params.put(WebExtClient.KEY_REQUEST, WebExtClient.KEY_REQUEST_EDIT_FLIGHT);
		params.put("time", Long.toString(System.currentTimeMillis()));	
		
		// POST
	    long backoff = ServerUtilities.BACKOFF_MILLI_SECONDS + ServerUtilities.random.nextInt(1000);
		for (int i=0; i < ServerUtilities.MAX_ATTEMPTS; i++) {  
			
			try {
				int status = ServerUtilities.post(WebExtClient.getInstance(this).getMobileUserRestService(), params);
				if (status == 200) return "";
				
			} catch (IOException e) {
	            // should retry only on unrecoverable errors (like HTTP error code 503).
	            Log.e(TAG, "Failed to edit flight data on attempt " + i + ":" + e);
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
	                return WebExtClient.KEY_NO_CONNECTION;
	            }
	            // increase backoff exponentially
	            backoff *= 2;
	        } 
		}
		return WebExtClient.KEY_NO_CONNECTION;
	}

}
