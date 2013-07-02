package de.cargoonline.mobile; 
 
import de.cargoonline.mobile.push.CommonUtilities;
import de.cargoonline.mobile.push.ConnectionDetector;
import de.cargoonline.mobile.rest.ServerUtilities; 
import de.cargoonline.mobile.rest.WebExtClient; 
import de.cargoonline.mobile.uiutils.WakeLocker;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter; 
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

import com.google.android.gcm.GCMRegistrar;

public class StartActivity extends MainMenuActivity {
 
	private static boolean DEBUG_DEVICE_WITHOUT_REGISTRATION = false; 
	
    static { System.loadLibrary("iconv");  } 

    private static String TAG = "CO StartActivity";
    
    private ConnectionDetector cd;
    private Button startButton; 
    private Button showLastManifestButton; 
    private EditText nameEdit; 
    private String regId;  
    private String user;  
    
	 /**
     * Receiving push messages
     * */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
        	String newId = GCMRegistrar.getRegistrationId(c);
        	
        	if (newId == null || newId.equals("")) {
        		Log.d(TAG, "GCM reset successful! Restart app without debug option now.");
        		finish();
        		return;
        	}        		
        	
            //String newMessage = intent.getExtras().getString(ServerUtilities.EXTRA_MESSAGE);
            StartActivity a = (StartActivity) c;
            a.setWelcomeLayout();
            a.setRegId(newId);

            // Waking up mobile if it is sleeping
            WakeLocker.acquire(c); 
            
            // Releasing wake lock
            WakeLocker.release();
        }
        
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                
 
        user = prefs.getString(ServerUtilities.PROPERTY_USER_NAME, "");
        
        cd = new ConnectionDetector(getApplicationContext());
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not available
        	  warnInternetConnection();
        }
        
        setContentView(R.layout.activity_start); 
        nameEdit = (EditText)findViewById(R.id.your_name_edit);
               		
        if ((cd.isIceCreamSandwichOrAbove() || cd.hasGoogleAccount()) 
        	&& !DEBUG_DEVICE_WITHOUT_REGISTRATION) { 

            // Requirements for GCM registrations fulfilled
        	registerReceiver(mHandleMessageReceiver, new IntentFilter(CommonUtilities.DISPLAY_MESSAGE_ACTION)); 
        	 
        	
            // Make sure the device has the proper dependencies & manifest was properly set
            GCMRegistrar.checkDevice(this);       
            GCMRegistrar.checkManifest(this); 
                        
            // Get GCM registration id
            regId = GCMRegistrar.getRegistrationId(this); 
        	
            // Register at GCM
            if (regId.equals("")) { 
            	GCMRegistrar.register(this, CommonUtilities.SENDER_ID); 
            }
    	} else {
    		// PUSH Service not available (old devices without google account)
    		// register at web-ext only, without regid.
    		// App/User has to pull status 
    		Log.d(TAG, "GCM not available on this device! Continuing without registration.");
    		
    	}  
        setWelcomeLayout();
    }

    
    @Override
    public void onBackPressed() { 
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        finish();
        return;
    }    
  
    @Override
    protected void onDestroy() { 
        try {
            unregisterReceiver(mHandleMessageReceiver);
            GCMRegistrar.onDestroy(this);
        } catch (Exception e) {
             Log.d("UnRegister Receiver Error", "> " + e.getMessage());
        }
        super.onDestroy();
    }
    private void setWelcomeLayout() {
    	TextView tv1 = (TextView)findViewById(R.id.yourname_tv);
    	tv1.setVisibility(View.INVISIBLE);

    	String welcomeStr = "";
        if (user != null && !user.equals(""))
        	welcomeStr = getResources().getString(R.string.welcome_desc, user);
        else
        	welcomeStr = getResources().getString(R.string.welcome_new_user_desc);
                
        TextView tv2 = (TextView)findViewById(R.id.welcome_desc_tv);
        tv2.setText(welcomeStr);    	

        nameEdit.setVisibility(View.INVISIBLE);
    	
        ProgressBar pb = (ProgressBar)findViewById(R.id.firststart_pb);
        pb.setVisibility(View.GONE);
        
        startButton = (Button)findViewById(R.id.startScanButton);
    	startButton.setVisibility(View.VISIBLE);     	 
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
    			startScanner();
    			startButton.setBackgroundResource(R.drawable.button_bg_pressed);
			}
        });

        showLastManifestButton = (Button)findViewById(R.id.showLastManifestButton);
        if (hasSavedManifestData()) {
	        showLastManifestButton.setVisibility(View.VISIBLE);        	 
			showLastManifestButton.setBackgroundResource(R.drawable.button_bg_pressed);
	        showLastManifestButton.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					v.setBackgroundResource(R.drawable.button_bg_red);
					return false;
				}        	
	        });
	        showLastManifestButton.setOnClickListener(new OnClickListener() {    		
	    		@Override
				public void onClick(View v) {  
	    			if (!loadSavedManifestData()) 
	    				Toast.makeText(v.getContext(), R.string.invalid_last_manifest_error, Toast.LENGTH_SHORT).show();
	    			 
	    			showLastManifestButton.setBackgroundResource(R.drawable.button_bg_pressed);
				}
	        });
        } else
	        showLastManifestButton.setVisibility(View.GONE);        	 
    } 
   
     
    private boolean hasSavedManifestData() { 	
    	return (prefs.contains(WebExtClient.KEY_MANIFEST_ID) &&
    			prefs.contains(WebExtClient.KEY_MANIFEST_PWD) &&
    			prefs.contains(WebExtClient.KEY_SPEDITION_ID));
    } 
    
   
    
    public void setRegId(String id) {
    	this.regId = id;
    	super.setRegId(id);
    }  
}
