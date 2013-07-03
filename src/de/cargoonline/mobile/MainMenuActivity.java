package de.cargoonline.mobile;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;

import de.cargoonline.mobile.camera.QRScanActivity;
import de.cargoonline.mobile.manifest.DisplayActivity;
import de.cargoonline.mobile.manifest.GetManifestDataActivity;
import de.cargoonline.mobile.rest.ManifestDataService;
import de.cargoonline.mobile.rest.RegistrationService;
import de.cargoonline.mobile.rest.ServerUtilities;
import de.cargoonline.mobile.rest.SubmitManifestService;
import de.cargoonline.mobile.rest.WebExtClient;
import de.cargoonline.mobile.uiutils.ActivityRegistry;
import de.cargoonline.mobile.uiutils.AlertDialogManager;  
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class MainMenuActivity extends Activity {

   public static final String PREF_STORE = "CargoOnline";
   public static final String TAG = "CO MainMenuActivity";
   public static SharedPreferences prefs; 
   public static AlertDialogManager alert;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 

	    ActivityRegistry.register(this);
		prefs = getSharedPreferences(PREF_STORE, Context.MODE_PRIVATE);
		alert =  new AlertDialogManager();
	} 
	 
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) { 
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        
        if (StartActivity.DEBUG_ALLOW_UNREGISTER) {
        	MenuItem unregisterItem = (MenuItem) this.findViewById(R.id.menu_unregister);
        	unregisterItem.setVisible(true);
        }
        return true;
    }
	    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {        
        switch (item.getItemId()) {
        	
        	// TODO: switch name
            case R.id.menu_scan:
    			startScanner();
                return true;
                
            case R.id.menu_last_manifest:
            	loadSavedManifestData();
                return true;
                
            case R.id.menu_about:
                showAboutDialog();
                return true;
              
            
            case R.id.menu_unregister: 
            	if (StartActivity.DEBUG_ALLOW_UNREGISTER) unregister();
                return true;
            
            case R.id.menu_exit:
            	ActivityRegistry.finishAll();
            	return true;
            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	    
	    /**
	     * for debugging purposes (enable unregister from GCM/Prefs)            
	     */
	    protected void unregister() {
	    	GCMRegistrar.unregister(this);  
	    	Editor e = prefs.edit();
	    	e.clear(); 
	    	e.commit();    
	    }
	    
	    public boolean loadSavedManifestData() {  
			String lastManifestId = prefs.getString(WebExtClient.KEY_MANIFEST_ID, "");
			String lastSpedId = prefs.getString(WebExtClient.KEY_SPEDITION_ID, "");
			String lastManifestPwd = prefs.getString(WebExtClient.KEY_MANIFEST_PWD, "");
			if (!lastManifestId.equals("") && !lastSpedId.equals("") && !lastManifestPwd.equals("")) {
				startManifestActivity(lastManifestId, lastSpedId, lastManifestPwd, false); 
				return true;
			} else 
				return false;
	    } 
	    public void warnInternetConnection() { 
	    	warn(R.string.internet_connection_error, R.string.internet_connection_error_desc); 
	    } 
	    
	    public void warn(int title, int desc) { 
	    	alert.setContext(this);
            alert.showAlertDialog(
        		getResources().getString(title),
        		getResources().getString(desc), 
        		R.drawable.fail); 
	    } 
	    
	    protected void showAboutDialog() {  
	    	String menuText = getResources().getString(R.string.version_name) + "\n\n";
	    	menuText += getResources().getString(R.string.company) + "\n\n";
	    	menuText += getResources().getString(R.string.ust_id_title) + " " + getResources().getString(R.string.ust_id) + "\n\n";
	    	menuText += getResources().getString(R.string.website);
	    	
	    	alert.setContext(this);
            alert.showAlertDialog(
        		getResources().getString(R.string.app_name),
        		menuText, 
        		R.drawable.ic_launcher); 
	    } 

	    protected void startScanner() {
	    	Intent i = new Intent(this, QRScanActivity.class); 
			startActivity(i);
	    }
	    
	    public void startManifestActivity(String manifestId, String speditionId, String manifestPwd, boolean noRegistrationPossible) {
	    	Intent i = new Intent(this, GetManifestDataActivity.class);
			i.putExtra(WebExtClient.KEY_MANIFEST_ID, manifestId);
	    	i.putExtra(WebExtClient.KEY_SPEDITION_ID, speditionId);
			i.putExtra(WebExtClient.KEY_MANIFEST_PWD, manifestPwd); 
			i.putExtra(WebExtClient.KEY_NO_REGISTRATION, noRegistrationPossible);
			startActivity(i); 
	    }
	    
	    protected void startRegistrationService(String userName, String regid, String manifestId, String speditionId, String manifestPwd) {
	    	Intent registerIntent = new Intent(this, RegistrationService.class);
	    	
	    	if (manifestId != null) {
	    		registerIntent.putExtra(WebExtClient.KEY_MANIFEST_ID, manifestId);
	    		registerIntent.putExtra(WebExtClient.KEY_SPEDITION_ID, speditionId);
	    		registerIntent.putExtra(WebExtClient.KEY_MANIFEST_PWD, manifestPwd); 
	    	}	    	
			registerIntent.putExtra(ServerUtilities.PROPERTY_USER_NAME, userName);
			registerIntent.putExtra(ServerUtilities.PROPERTY_REG_ID, regid); 
			startService(registerIntent); 
	    }
	    
	    protected void startRegistrationService(String userName, String regid) {
	    	startRegistrationService(userName, regid, null, null, null);
	    }
	    
	    public void startManifestDataService(String action) {
	    	Intent loadIntent = new Intent(this, ManifestDataService.class);
	    	loadIntent.putExtra(WebExtClient.KEY_REQUEST, action);
			startService(loadIntent); 
	    }
	   
	    protected void startSubmitService(ArrayList<String> mrns, String speditionId, String manifestId) {
	    	Intent submitIntent = new Intent(this, SubmitManifestService.class);
	    	submitIntent.putExtra(ManifestDataService.KEY_POSITIONS, mrns);
	    	submitIntent.putExtra(WebExtClient.KEY_MANIFEST_ID, manifestId);
	    	submitIntent.putExtra(WebExtClient.KEY_SPEDITION_ID, speditionId);
	    	startService(submitIntent);
	    } 
	    
	    protected void setRegId(String id) {
	    	Editor editor = prefs.edit();
	    	editor.putString(ServerUtilities.PROPERTY_REG_ID, id);
	    	editor.commit();
	    }  
	    
	    public void loadManifest(String result) {  
    		try {     			
                JSONObject json = new JSONObject(result); 
                JSONArray jsonPositions = json.getJSONArray(ManifestDataService.KEY_POSITIONS); 
                
                Log.i(TAG,"<jsonobject>\n" + json.toString() + "\n</jsonobject>");
    			Intent i = new Intent(this, DisplayActivity.class);  
    			i.putExtra(ManifestDataService.KEY_SPEDITION_NAME, json.getString(ManifestDataService.KEY_SPEDITION_NAME));
    			i.putExtra(ManifestDataService.KEY_MRN_NO, getManifestItems(jsonPositions, ManifestDataService.KEY_MRN_NO));
    			i.putExtra(ManifestDataService.KEY_DETAIL_TEXT, getManifestItems(jsonPositions, ManifestDataService.KEY_DETAIL_TEXT));
        		i.putExtra(ManifestDataService.KEY_STATUS, getManifestItems(jsonPositions, ManifestDataService.KEY_STATUS));
        		i.putExtra(ManifestDataService.KEY_FLIGHT_NO, getManifestItems(jsonPositions, ManifestDataService.KEY_FLIGHT_NO));
        		i.putExtra(ManifestDataService.KEY_FLIGHT_LOCATION, getManifestItems(jsonPositions, ManifestDataService.KEY_FLIGHT_LOCATION));
        		i.putExtra(ManifestDataService.KEY_EORI_NO[0], getManifestItems(jsonPositions, ManifestDataService.KEY_EORI_NO, " / "));
        		i.putExtra(ManifestDataService.KEY_AWB_NO[1], getManifestItems(jsonPositions, ManifestDataService.KEY_AWB_NO, "-"));
        		i.putExtra(WebExtClient.KEY_SPEDITION_ID, prefs.getString(WebExtClient.KEY_SPEDITION_ID, ""));
        		i.putExtra(WebExtClient.KEY_MANIFEST_ID, prefs.getString(WebExtClient.KEY_MANIFEST_ID, ""));
    			startActivity(i); 
        	} catch (JSONException e) { 
    			
        		// invalid data, try again, show camera
        		Log.e(TAG, e.getMessage());
        		Log.e(TAG, e.getStackTrace().toString());
        		
        		Intent i = new Intent(this, StartActivity.class);
            	startActivity(i); 
            	Toast.makeText(this, R.string.invalid_scan_result_error, Toast.LENGTH_LONG).show(); 
    		} 
        }  
	    
        private String[] getManifestItems(JSONArray json, String key) throws JSONException { 
    		
    		int positionCount = json.length();		
    		String[] result = new String[positionCount];
    		
    		for (int i=0; i<positionCount; i++) {
    			JSONObject position = json.getJSONObject(i);
    			result[i] = position.getString(key);
    		} 
        	return result;
        }
        
        private String[] getManifestItems(JSONArray json, String[] keys, String delimiter) throws JSONException { 
    		
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
