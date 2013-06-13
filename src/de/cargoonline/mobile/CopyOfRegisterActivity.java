package de.cargoonline.mobile;

import com.google.android.gcm.GCMRegistrar;

import de.cargoonline.mobile.camera.QRScanActivity; 
import de.cargoonline.mobile.rest.WebExtClient;
import de.cargoonline.mobile.uiutils.CommonIntents;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CopyOfRegisterActivity extends Activity {
	
    private static String TAG = "CO Start";
    private EditText nameEdit; 
    private Bundle manifestBundle;
    private Button startButton;
    private SharedPreferences prefs;
    private String regid;
    private AsyncTask<Void, Void, String> mRegisterTask;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                
        manifestBundle = getIntent().getExtras();
        
    	prefs = getSharedPreferences(CommonIntents.PREF_STORE, Context.MODE_PRIVATE);
 		regid = GCMRegistrar.getRegistrationId(this);  
        
        setContentView(R.layout.activity_start);        
        startButton = (Button)findViewById(R.id.startScanButton);
        nameEdit = (EditText)findViewById(R.id.your_name_edit);   
        
        setLayout(false);
    }
    
    @Override
    public void onBackPressed() { 
    	Intent i = new Intent(getApplicationContext(), QRScanActivity.class);
    	startActivity(i);
        finish();
        return;
    }
    
    @Override
    protected void onDestroy() {
        if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        } 
        super.onDestroy();
    }
    /*
    public void registerBackground(final String userName) {
    	mRegisterTask = new AsyncTask<Void, Void, String>() {    		
			 @Override
	         protected String doInBackground(Void... params) {
				 if (regid.equals("")) {
					 // GCM registration not finished.
					 Log.d(TAG, "No GCM registration!");
					 this.cancel(true);
					 return null;
				 } 
				 String gMail = WebExtClient.getGoogleAccount(getApplicationContext());
				 String phoneNo = WebExtClient.getPhoneNumber(getApplicationContext());
	             ServerUtilities.register(getApplicationContext(), 
	            		 userName, 
	            		 gMail, 
	            		 phoneNo,
	            		 regid);
	             Log.d(TAG, "Registration at web-ext. Registration ID=" +regid+ ", Name="+userName + ", gmail=" +gMail+ ", phone="+phoneNo);
	              
	             
	             return userName;
	         }

             @Override
             protected void onPostExecute(String result) {
            	 mRegisterTask = null;    
            	 saveUserPreferences(result);          	 
             }	 
         };
         mRegisterTask.execute(null, null, null);
    }
   
    

    private void saveUserPreferences(String userName) {
    	// Save the regid for future use - no need to register again.
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ServerUtilities.PROPERTY_REG_ID, regid);
        
        // also save user name:
        editor.putString(ServerUtilities.PROPERTY_USER_NAME, userName);
   
        // and google account, phone number (if available)
        String gMailAccount = WebExtClient.getGoogleAccount(this);
        String phoneNo = WebExtClient.getPhoneNumber(this);
        
        editor.putString(ServerUtilities.PROPERTY_USER_GOOGLEMAIL, gMailAccount);
        editor.putString(ServerUtilities.PROPERTY_USER_PHONE, phoneNo);

        editor.commit(); 
        Log.d(TAG, "Saved Preferences: RegId=" +regid+ ", Name="+userName + ", gmail=" +gMailAccount+ ", phone="+phoneNo);
        Log.d(TAG, "Now Starting Manifest Activity...");
        
    	 CommonIntents.startManifestActivity(this, 
    			 manifestBundle.getString(WebExtClient.KEY_MANIFEST_ID),
    			 manifestBundle.getString(WebExtClient.KEY_SPEDITION_ID),
    			 manifestBundle.getString(WebExtClient.KEY_MANIFEST_PWD));
    }
    */
    private void setLayout(boolean locked) {
    	TextView tv1 = (TextView)findViewById(R.id.yourname_tv);     
    	tv1.setVisibility(View.VISIBLE);       
    	
        TextView tv2 = (TextView)findViewById(R.id.welcome_desc_tv);        
        tv2.setText(locked ? R.string.register_desc_waiting : R.string.register_desc);   

    	nameEdit.setVisibility(View.VISIBLE);    
    	nameEdit.setEnabled(!locked);
        
        ProgressBar pb = (ProgressBar)findViewById(R.id.firststart_pb);
        pb.setVisibility(locked ? View.VISIBLE : View.GONE);    
        
        startButton = (Button)findViewById(R.id.startScanButton);    	
        startButton.setText(R.string.register);
    	startButton.setVisibility(locked ? View.GONE : View.VISIBLE);                	 
        startButton.setBackgroundResource(R.drawable.button_bg_pressed); 
        startButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.setBackgroundResource(R.drawable.button_bg_red);
				return false;
			}        	
        });
    	startButton.setOnClickListener(new OnClickListener() {    		
    		@Override
			public void onClick(View v) {
				Context ctx = v.getContext();
		        
				// register at WEB-EXT		
	            String userName = nameEdit.getText().toString();
				if (userName.trim().length() == 0) {
					Toast.makeText(ctx, R.string.register_enter_name_reminder, Toast.LENGTH_LONG).show();
				
				} else if (regid.equals("")) { 
					Toast.makeText(ctx, R.string.register_gcm_warning, Toast.LENGTH_LONG).show(); 
					continueManifest();
				} else {
					setLayout(true); 

					 WebExtClient webExt = WebExtClient.getInstance(getApplicationContext());
					/* if (webExt.autoRegister(getApplicationContext(), userName, regid)) {
						 Log.d(TAG, "Now Starting Manifest Activity...");					        
				    	 continueManifest(); // TODO zu früh! warte auf registrierung
					 } */
				} 
		        startButton.setBackgroundResource(R.drawable.button_bg_pressed);				
    		}
        });
    } 
    
    public void continueManifest() {   						
		CommonIntents.startManifestActivity(this,
 			 manifestBundle.getString(WebExtClient.KEY_MANIFEST_ID),
 			 manifestBundle.getString(WebExtClient.KEY_SPEDITION_ID),
 			 manifestBundle.getString(WebExtClient.KEY_MANIFEST_PWD));
    }
     
	
}
