package de.cargoonline.mobile.rest; 

import de.cargoonline.mobile.MainMenuActivity;
import de.cargoonline.mobile.R;
import de.cargoonline.mobile.manifest.DisplayActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 *  handle responses to actions: "registration complete", "manifest loaded", "manifest submitted"
 *
 */
public class COServiceReceiver extends BroadcastReceiver {
	public static final String TAG = "CO COServiceReceiver";
	public static final String ACTION_REGISTER = "de.cargoonline.mobile.intent.action.USER_REGISTERED";
	public static final String ACTION_LOAD = "de.cargoonline.mobile.intent.action.MANIFEST_LOADED";
	public static final String ACTION_RELOAD = "de.cargoonline.mobile.intent.action.MANIFEST_RELOADED";
    public static final String ACTION_SUBMIT = "de.cargoonline.mobile.intent.action.MANIFEST_SUBMITTED";
    public static final String ACTION_EDIT_FLIGHT = "de.cargoonline.mobile.intent.action.FLIGHT_EDITED";
		   
 	@Override
 	public void onReceive(Context context, Intent intent) {
	   	Log.d(TAG, "Broadcast Received"); 
	   	
	   	MainMenuActivity target = (MainMenuActivity) context;
	    
	    if (intent.getBooleanExtra(WebExtClient.KEY_NO_CONNECTION, false) == true) { 
		   	Log.d(TAG, "No internet connection. Warn and continue...");	 
		   	target.warnInternetConnection();
	    } 
	    if (intent.getBooleanExtra(WebExtClient.KEY_INPUT_ERROR, false) == true) { 
		   	Log.d(TAG, "Wrong user input. Warn and continue...");	 
		   	target.warn(R.string.user_input_error, R.string.user_input_error_desc);
	    } 
	    
	    String action =	intent.getAction();
 		    
 		if (action.equals(ACTION_REGISTER)) { 
	 	    Log.d(TAG, "Registration checked. Now loading manifest data...");	 	    
	 	    
	 	    // registration directly from register mask: start next activity
	 	    String manifestId = intent.getStringExtra(WebExtClient.KEY_MANIFEST_ID);
	 	    if (manifestId != null && !manifestId.equals("")) {
	 	    	target.startManifestActivity(
		 	 			manifestId,
		 	 			intent.getStringExtra(WebExtClient.KEY_SPEDITION_ID),
		 	 			intent.getStringExtra(WebExtClient.KEY_MANIFEST_PWD));
	 	    } else {
	 	    	// auto registration: just start next service
	 	    	target.startManifestDataService(COServiceReceiver.ACTION_LOAD); 
 			}	 			
	 		
	    } else if (action.equals(ACTION_LOAD) || action.equals(ACTION_RELOAD)) {
	   		Log.d(TAG, "Manifest data loaded from server. Now Starting Manifest Activity...");
	   		String manifestData = intent.getStringExtra(WebExtClient.KEY_RESPONSE);
	   		target.loadManifest(manifestData);
	   	 
	    } else if (action.equals(ACTION_SUBMIT)) {
	   		Log.d(TAG, "Manifest data submitted sucessfully. Now reloading manifest...");
	   		target.startManifestDataService(COServiceReceiver.ACTION_RELOAD); 
	    }
	    else if (action.equals(ACTION_EDIT_FLIGHT)) {
	   		Log.d(TAG, "Flight data edited sucessfully. Now reloading manifest...");
	   	
	   		try {
	   			DisplayActivity dTarget = (DisplayActivity) target;
	   			dTarget.refreshState();
	   		} catch (ClassCastException e) {
	   			target.loadSavedManifestData();
	   		}
	    }
    }
}
