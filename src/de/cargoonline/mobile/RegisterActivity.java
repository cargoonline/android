package de.cargoonline.mobile;

import com.google.android.gcm.GCMRegistrar;

import de.cargoonline.mobile.camera.QRScanActivity; 
import de.cargoonline.mobile.rest.WebExtClient;
import de.cargoonline.mobile.uiutils.CommonIntents;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class RegisterActivity extends Activity {
	
    private static String TAG = "CO Start";
    private EditText nameEdit; 
    private Bundle manifestBundle;
    private Button startButton;
    private String regid;
    private RegisterReceiver receiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                
        manifestBundle = getIntent().getExtras();
        
 		regid = GCMRegistrar.getRegistrationId(this);  
        
        setContentView(R.layout.activity_start);        
        startButton = (Button)findViewById(R.id.startScanButton);
        nameEdit = (EditText)findViewById(R.id.your_name_edit);   
        
        // register broadcast receiver to handle auto register completion messages from WebExtService
        IntentFilter filter = new IntentFilter(RegisterReceiver.ACTION_REGISTER);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new RegisterReceiver();
        registerReceiver(receiver, filter);
        
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
        unregisterReceiver(receiver);
        super.onDestroy();
    }
    
    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }
    
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
					CommonIntents.checkRegistration(ctx, userName, regid);
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
    
    // respond to "registration complete" response
    public class RegisterReceiver extends BroadcastReceiver {
	   public static final String ACTION_REGISTER =
	      "de.cargoonline.mobile.intent.action.MESSAGE_PROCESSED";
	   @Override
	    public void onReceive(Context context, Intent intent) {
		   
		   if (intent.getBooleanExtra(WebExtClient.KEY_NO_CONNECTION, false) == true) { 
			   	Log.d(TAG, "No internet connection. Warn and continue...");		  
			   	CommonIntents.warnInternetConnection(context);
		   } else 		   
		   		Log.d(TAG, "Auto Registration successful. Now Starting Manifest Activity...");					        
	        
		   continueManifest(); 
	    }
	}
     
	
}
