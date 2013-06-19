package de.cargoonline.mobile.uiutils;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent; 
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import de.cargoonline.mobile.GetManifestDataActivity;
import de.cargoonline.mobile.R;
import de.cargoonline.mobile.StartActivity;
import de.cargoonline.mobile.manifest.DisplayActivity;
import de.cargoonline.mobile.rest.ManifestDataService;
import de.cargoonline.mobile.rest.RegistrationService;
import de.cargoonline.mobile.rest.ServerUtilities;
import de.cargoonline.mobile.rest.SubmitManifestService;
import de.cargoonline.mobile.rest.WebExtClient;

public class CommonIntents {
	   public static final String PREF_STORE = "CargoOnline";
	   public static final String TAG = "CO CommonIntents";
	   
	   public static final AlertDialogManager alert = new AlertDialogManager();
	  
	   public static void startManifestActivity(Context c, String manifestId, String speditionId, String manifestPwd) {
	    	Intent i = new Intent(c, GetManifestDataActivity.class);
			i.putExtra(WebExtClient.KEY_MANIFEST_ID, manifestId);
	    	i.putExtra(WebExtClient.KEY_SPEDITION_ID, speditionId);
			i.putExtra(WebExtClient.KEY_MANIFEST_PWD, manifestPwd); 
			c.startActivity(i); 
	    }
	    
	    public static void warnInternetConnection(Context c) { 
	    	alert.setContext(c);
            alert.showAlertDialog(
        		c.getResources().getString(R.string.internet_connection_error),
        		c.getResources().getString(R.string.internet_connection_error_desc), 
        		R.drawable.fail); 
	    } 
	    
	    public static void startRegistrationService(Context c, String userName, String regid, String manifestId, String speditionId, String manifestPwd) {
	    	Intent registerIntent = new Intent(c, RegistrationService.class);
	    	
	    	if (manifestId != null) {
	    		registerIntent.putExtra(WebExtClient.KEY_MANIFEST_ID, manifestId);
	    		registerIntent.putExtra(WebExtClient.KEY_SPEDITION_ID, speditionId);
	    		registerIntent.putExtra(WebExtClient.KEY_MANIFEST_PWD, manifestPwd); 
	    	}	    	
			registerIntent.putExtra(ServerUtilities.PROPERTY_USER_NAME, userName);
			registerIntent.putExtra(ServerUtilities.PROPERTY_REG_ID, regid); 
			c.startService(registerIntent); 
	    }
	    
	    public static void startRegistrationService(Context c, String userName, String regid) {
	    	startRegistrationService(c, userName, regid, null, null, null);
	    }
	    
	    public static void startManifestDataService(Context c, String action) {
	    	Intent loadIntent = new Intent(c, ManifestDataService.class);
	    	loadIntent.putExtra(WebExtClient.KEY_REQUEST, action);
			c.startService(loadIntent); 
	    }
	    
	    public static void startSubmitService(Context c, ArrayList<String> mrns, String speditionId, String manifestId) {
	    	Intent submitIntent = new Intent(c, SubmitManifestService.class);
	    	submitIntent.putExtra(ManifestDataService.KEY_POSITIONS, mrns);
	    	submitIntent.putExtra(WebExtClient.KEY_MANIFEST_ID, manifestId);
	    	submitIntent.putExtra(WebExtClient.KEY_SPEDITION_ID, speditionId);
	    	c.startService(submitIntent);
	    }

	    //public static void reloadManifest(Context c, String result) throws JSONException {
	    	  // TODO: nur liste aktualisieren, nicht ganze activity
	    //}
	    
	    public static void loadManifest(Context c, String result) {  
    		try {     			
                JSONObject json = new JSONObject(result); 
                JSONArray jsonPositions = json.getJSONArray(ManifestDataService.KEY_POSITIONS);
                SharedPreferences prefs = c.getSharedPreferences(CommonIntents.PREF_STORE, Context.MODE_PRIVATE);
                
                Log.i(TAG,"<jsonobject>\n" + json.toString() + "\n</jsonobject>");
    			Intent i = new Intent(c, DisplayActivity.class);  
    			i.putExtra(ManifestDataService.KEY_SPEDITION_NAME, json.getString(ManifestDataService.KEY_SPEDITION_NAME));
    			i.putExtra(ManifestDataService.KEY_MRN_NO, getManifestItems(jsonPositions, ManifestDataService.KEY_MRN_NO));
        		i.putExtra(ManifestDataService.KEY_STATUS, getManifestItems(jsonPositions, ManifestDataService.KEY_STATUS));
        		i.putExtra(ManifestDataService.KEY_FLIGHT_NO, getManifestItems(jsonPositions, ManifestDataService.KEY_FLIGHT_NO));
        		i.putExtra(ManifestDataService.KEY_FLIGHT_LOCATION, getManifestItems(jsonPositions, ManifestDataService.KEY_FLIGHT_LOCATION));
        		i.putExtra(ManifestDataService.KEY_EORI_NO[0], getManifestItems(jsonPositions, ManifestDataService.KEY_EORI_NO, " / "));
        		i.putExtra(ManifestDataService.KEY_AWB_NO[1], getManifestItems(jsonPositions, ManifestDataService.KEY_AWB_NO, "-"));
        		i.putExtra(WebExtClient.KEY_SPEDITION_ID, prefs.getString(WebExtClient.KEY_SPEDITION_ID, ""));
        		i.putExtra(WebExtClient.KEY_MANIFEST_ID, prefs.getString(WebExtClient.KEY_MANIFEST_ID, ""));
    			c.startActivity(i); 
        	} catch (JSONException e) { 
    			
        		// invalid data, try again, show camera
        		Log.e(TAG, e.getMessage());
        		Log.e(TAG, e.getStackTrace().toString());
        		
        		Intent i = new Intent(c, StartActivity.class);
            	c.startActivity(i); 
            	Toast.makeText(c, R.string.invalid_scan_result_error, Toast.LENGTH_LONG).show(); 
    		} 
        }  
	    
        private static String[] getManifestItems(JSONArray json, String key) throws JSONException { 
    		
    		int positionCount = json.length();		
    		String[] result = new String[positionCount];
    		
    		for (int i=0; i<positionCount; i++) {
    			JSONObject position = json.getJSONObject(i);
    			result[i] = position.getString(key);
    		} 
        	return result;
        }
        
        private static String[] getManifestItems(JSONArray json, String[] keys, String delimiter) throws JSONException { 
    		
    		int positionCount = json.length();
    		String[] result = new String[positionCount];
    		
    		for (int i=0; i<positionCount; i++) {
    			String curPos = "";
    			String curDelimiter = ""; 
    			JSONObject position = json.getJSONObject(i);
    			for (int j=0; j<keys.length; j++) {
    				curPos += curDelimiter + position.getString(keys[j]);
    				curDelimiter = delimiter;
    			}
    			result[i] = curPos;
    		} 
        	return result;
        }
	    
}
