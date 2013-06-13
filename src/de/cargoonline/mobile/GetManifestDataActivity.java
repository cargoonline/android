package de.cargoonline.mobile;
 

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
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
import de.cargoonline.mobile.rest.ServerUtilities;
import de.cargoonline.mobile.rest.WebExtClient;
import de.cargoonline.mobile.uiutils.CommonIntents;

public class GetManifestDataActivity extends Activity { 

	public final static String TAG = "CO GetManifestDataActivity";
	
	public final static String[] KEY_EORI_NO = {"eori_nr","nl_nr"};
	public final static String[] KEY_AWB_NO = {"mawb_prefix","mawb_nr"};
	public final static String KEY_MRN_NO = "mrn_nr";
	public final static String KEY_FLIGHT_NO = "extinf_befoerderm_kz";
	public final static String KEY_FLIGHT_LOCATION = "extinf_befoerderm_ladeort";
	public final static String KEY_STATUS = "b_status";
	public final static String KEY_SPEDITION_NAME = "spedition_name";
	public final static String KEY_POSITIONS = "positions";
	
	private String manifestID;
	private String speditionID; 
	private String manifestPwd; 
	private TextView manifestIdTextView;
	private TextView speditionIdTextView; 
	private SharedPreferences prefs;
	private RegisterReceiver receiver;
	
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
            finish();
        }
    	
		if (!isRegisteredOnDevice()) {
			// not even registered on device. send user to registration mask first.
    		Intent i = new Intent(this, RegisterActivity.class);
    		i.putExtra(WebExtClient.KEY_MANIFEST_ID, manifestID);
    		i.putExtra(WebExtClient.KEY_SPEDITION_ID, speditionID);
    		i.putExtra(WebExtClient.KEY_MANIFEST_PWD, manifestPwd);
    		startActivity(i); 
    		finish();
		}
 
		// we are registered on GCM and device.
		// so, let's ask the web-ext service: am i registered?  
        // register broadcast receiver to handle auto register completion messages from WebExtService
        IntentFilter filter = new IntentFilter(RegisterReceiver.ACTION_REGISTER);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new RegisterReceiver();
        registerReceiver(receiver, filter);
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
		CommonIntents.checkRegistration(getApplicationContext(), null, null);
		
    }
    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }
    
    
    private boolean isRegisteredOnDevice() {
    	String user = prefs.getString(ServerUtilities.PROPERTY_USER_NAME, "");
    	String gcmRegID = prefs.getString(ServerUtilities.PROPERTY_REG_ID, "");
    	if (user.equals("") || gcmRegID.equals("")) return false; // cannot be registered - has not even seen the registration mask in his life 
   
    	return true;
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
    
    
    // respond to "registration complete" response
    public class RegisterReceiver extends BroadcastReceiver {
 	   public static final String ACTION_REGISTER =
 	      "de.cargoonline.mobile.intent.action.MESSAGE_PROCESSED";
 	   @Override
 	    public void onReceive(Context context, Intent intent) {
 		   	Log.d(TAG, "Registration Broadcast Received"); 
 		   	
 		   if (intent.getBooleanExtra(WebExtClient.KEY_NO_CONNECTION, false) == true) { 
			   	Log.d(TAG, "No internet connection. Warn and continue...");		  
			   	CommonIntents.warnInternetConnection(context);
		   } 
 		   	
 			new GetManifestDataThread().execute(); 	       
 	    }
 	}
    
private class GetManifestDataThread extends AsyncTask <Void, Void, String> { 
    	
    	public GetManifestDataThread() {
    		super();
    	}
    	
    	@Override
    	protected String doInBackground(Void... params) { 
    		
    		Log.d(TAG, "Registration checked. Now loading manifest data.");
    		return WebExtClient.getInstance(getApplicationContext())
        			.getManifestData(speditionID, manifestID, manifestPwd); 
    	}
    	
    	@Override
    	protected void onPostExecute(String result) {  
            Context c = getApplicationContext();
    		try {     			
                JSONObject json = new JSONObject(result); 
                JSONArray jsonPositions = json.getJSONArray(KEY_POSITIONS);

                Log.i(TAG, "Manifest data loaded.");
                Log.i(TAG,"<jsonobject>\n" + json.toString() + "\n</jsonobject>");
    			Intent i = new Intent(c, DisplayActivity.class);  
    			i.putExtra(KEY_SPEDITION_NAME, json.getString(KEY_SPEDITION_NAME));
    			i.putExtra(KEY_MRN_NO, getManifestItems(jsonPositions, KEY_MRN_NO));
        		i.putExtra(KEY_STATUS, getManifestItems(jsonPositions, KEY_STATUS));
        		i.putExtra(KEY_FLIGHT_NO, getManifestItems(jsonPositions, KEY_FLIGHT_NO));
        		i.putExtra(KEY_FLIGHT_LOCATION, getManifestItems(jsonPositions, KEY_FLIGHT_LOCATION));
        		i.putExtra(KEY_EORI_NO[0], getManifestItems(jsonPositions, KEY_EORI_NO, " / "));
        		i.putExtra(KEY_AWB_NO[1], getManifestItems(jsonPositions, KEY_AWB_NO, "-"));
        		i.putExtra(WebExtClient.KEY_SPEDITION_ID, speditionID);
        		i.putExtra(WebExtClient.KEY_MANIFEST_ID, manifestID);
    			startActivity(i); 
        	} catch (JSONException e) { 
    			
        		// invalid data, try again, show camera
        		Log.e(TAG, e.getMessage());
        		Log.e(TAG, e.getStackTrace().toString());
        		
        		Intent i = new Intent(c, StartActivity.class);
            	startActivity(i); 
            	Toast.makeText(c, R.string.invalid_scan_result_error, Toast.LENGTH_LONG).show();
                finish();
    		} 
        }     	 
	}
      
}
