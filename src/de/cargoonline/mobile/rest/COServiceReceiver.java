package de.cargoonline.mobile.rest;

import de.cargoonline.mobile.uiutils.CommonIntents;
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
		   
 	@Override
 	public void onReceive(Context context, Intent intent) {
	   	Log.d(TAG, "Registration Broadcast Received"); 		    
	    
	    if (intent.getBooleanExtra(WebExtClient.KEY_NO_CONNECTION, false) == true) { 
		   	Log.d(TAG, "No internet connection. Warn and continue...");		  
		   	CommonIntents.warnInternetConnection(context);
	    } 
	    String action =	intent.getAction();
 		    
 		if (action.equals(ACTION_REGISTER)) { 
	 	    Log.d(TAG, "Registration checked. Now loading manifest data...");	 	    
	 	    
	 	    // registration directly from register mask: start next activity
	 	    String manifestId = intent.getStringExtra(WebExtClient.KEY_MANIFEST_ID);
	 	    if (manifestId != null && !manifestId.equals("")) {
	 	    	CommonIntents.startManifestActivity(context,
		 	 			manifestId,
		 	 			intent.getStringExtra(WebExtClient.KEY_SPEDITION_ID),
		 	 			intent.getStringExtra(WebExtClient.KEY_MANIFEST_PWD));
	 	    } else {
	 	    	// auto registration: just start next service
 				CommonIntents.startManifestDataService(context, COServiceReceiver.ACTION_LOAD); 
 			}	 			
	 		
	    } else if (action.equals(ACTION_LOAD) || action.equals(ACTION_RELOAD)) {
	   		Log.d(TAG, "Manifest data loaded from server. Now Starting Manifest Activity...");
	   		String manifestData = intent.getStringExtra(WebExtClient.KEY_RESPONSE);
	   		CommonIntents.loadManifest(context, manifestData);
	   	 
	    } else if (action.equals(ACTION_SUBMIT)) {
	   		Log.d(TAG, "Manifest data submitted sucessfully. Now reloading manifest...");
	   		CommonIntents.startManifestDataService(context, COServiceReceiver.ACTION_RELOAD); 
	    }
    }
}
