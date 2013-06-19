package de.cargoonline.mobile;
 
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import de.cargoonline.mobile.R; 
import de.cargoonline.mobile.rest.COServiceReceiver;
import de.cargoonline.mobile.rest.ServerUtilities;
import de.cargoonline.mobile.rest.WebExtClient;
import de.cargoonline.mobile.uiutils.CommonIntents;

public class GetManifestDataActivity extends Activity { 

	public final static String TAG = "CO GetManifestDataActivity";
	
	private String manifestID;
	private String speditionID; 
	private String manifestPwd; 
	private TextView manifestIdTextView;
	private TextView speditionIdTextView; 
	private SharedPreferences prefs;
	private COServiceReceiver receiver; 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_manifest_data); 

        manifestID = null;
        speditionID = null;
        manifestPwd = null;  
        manifestIdTextView = (TextView) findViewById(R.id.manifestIdTextView);
        speditionIdTextView = (TextView) findViewById(R.id.speditionIdTextView);
 
    	prefs = getSharedPreferences(CommonIntents.PREF_STORE, Context.MODE_PRIVATE); 
        
        if (getQRData(getIntent().getExtras())) { 
        	manifestIdTextView.setText(manifestID);
        	speditionIdTextView.setText(speditionID);   
        } else { 
        	// no valid manifest id on qr code. go back, try again...
        	Log.e(TAG, "ERROR: no manifest data from QR code!");
        	Intent i = new Intent(this, StartActivity.class);
        	startActivity(i); 
        	Toast.makeText(this, R.string.invalid_scan_result_error, Toast.LENGTH_LONG).show(); 
        }
    	
		if (!isRegisteredOnDevice()) {
        	Log.d(TAG, "User is not even registered on the device. Starting registration mask.");
    		Intent i = new Intent(this, RegisterActivity.class);
    		i.putExtra(WebExtClient.KEY_MANIFEST_ID, manifestID);
    		i.putExtra(WebExtClient.KEY_SPEDITION_ID, speditionID);
    		i.putExtra(WebExtClient.KEY_MANIFEST_PWD, manifestPwd);
    		startActivity(i);  
    		finish();
		}
		
		receiver = new COServiceReceiver();
 
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	Intent i = new Intent(this, StartActivity.class);
        	startActivity(i);
            return true;   
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onStart() { 
    	super.onStart();
		CommonIntents.startRegistrationService(getApplicationContext(), null, null);
		
    }
    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }
    
    @Override
    protected void onResume() {
		// we are registered on GCM and device.
		// so, let's ask the web-ext service: am i registered?  
        // register broadcast receiver to handle auto register completion messages from WebExtService 
                
        IntentFilter regfilter = new IntentFilter(COServiceReceiver.ACTION_REGISTER);
        regfilter.addCategory(Intent.CATEGORY_DEFAULT); 
        registerReceiver(receiver, regfilter); 

        // also listen to "manifest load" actions (needed later...)
	    IntentFilter manifestfilter = new IntentFilter(COServiceReceiver.ACTION_LOAD);
        manifestfilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, manifestfilter);
        super.onResume();
    }
    
    
    private boolean isRegisteredOnDevice() {
    	String user = prefs.getString(ServerUtilities.PROPERTY_USER_NAME, "");
    	String gcmRegID = prefs.getString(ServerUtilities.PROPERTY_REG_ID, "");
    	if (user.equals("") || gcmRegID.equals("")) return false; // cannot be registered - has not even seen the registration mask in his life 
   
    	return true;
    }
     
    
    private boolean getQRData(Bundle bundle) { 
    	if (!bundle.containsKey(WebExtClient.KEY_MANIFEST_ID) ||
    		!bundle.containsKey(WebExtClient.KEY_SPEDITION_ID) ||
    		!bundle.containsKey(WebExtClient.KEY_MANIFEST_PWD))
    			return false;
    	 
    	String value = bundle.getString(WebExtClient.KEY_MANIFEST_ID);
		if (value == null || value.equals("")) return false; 
		manifestID = value; 
    		 
		value = bundle.getString(WebExtClient.KEY_SPEDITION_ID);
		if (value == null || value.equals("")) return false; 
		speditionID = value; 

		value = bundle.getString(WebExtClient.KEY_MANIFEST_PWD);
		if (value == null || value.equals("")) return false; 
		manifestPwd = value; 
		 
    	return savePrefsLastManifestData();
    }

    public boolean savePrefsLastManifestData() {
    	SharedPreferences prefs = getSharedPreferences(CommonIntents.PREF_STORE, Context.MODE_PRIVATE);
 		Editor e = prefs.edit();

 		e.putString(WebExtClient.KEY_MANIFEST_ID, manifestID);
 		e.putString(WebExtClient.KEY_SPEDITION_ID, speditionID);
 		e.putString(WebExtClient.KEY_MANIFEST_PWD, manifestPwd); 
 		e.commit();
 		 
    	return true;
    }
 
}
