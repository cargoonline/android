package de.cargoonline.mobile.rest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map; 

import de.cargoonline.mobile.uiutils.CommonIntents; 
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

public class RegistrationService extends IntentService {

	public static final String TAG = "CO RegistrationService";

	private SharedPreferences prefs;
		
	public RegistrationService() { 
		super("RegistrationService");
	}  

	@Override
	protected void onHandleIntent(Intent intent) {	

    	Log.d(TAG, "WEB-EXT Registration Service Started...");
		prefs = getSharedPreferences(CommonIntents.PREF_STORE, Context.MODE_PRIVATE);

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(COServiceReceiver.ACTION_REGISTER);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		
		try {
			if (!isRegistered()) {		
				String username = intent.getStringExtra(ServerUtilities.PROPERTY_USER_NAME);
				String regId = intent.getStringExtra(ServerUtilities.PROPERTY_REG_ID);
				
				if (username != null && regId != null) autoRegister(username, regId);
				else autoRegister(); 
			}
		} catch (IOException e) {
			broadcastIntent.putExtra(WebExtClient.KEY_NO_CONNECTION, true);			
		}
		
		String manifestId = intent.getStringExtra(WebExtClient.KEY_MANIFEST_ID);
		if (manifestId != null && !manifestId.equals("")) {
			broadcastIntent.putExtra(WebExtClient.KEY_MANIFEST_ID, manifestId);
			broadcastIntent.putExtra(WebExtClient.KEY_MANIFEST_PWD, intent.getStringExtra(WebExtClient.KEY_MANIFEST_PWD));
			broadcastIntent.putExtra(WebExtClient.KEY_SPEDITION_ID, intent.getStringExtra(WebExtClient.KEY_SPEDITION_ID));
		}
		sendBroadcast(broadcastIntent);		
	}
	    
    public boolean isRegistered() throws IOException { 
    	String regId = prefs.getString(ServerUtilities.PROPERTY_REG_ID, "");
    	String user = prefs.getString(ServerUtilities.PROPERTY_USER_NAME, "");
    	String email = prefs.getString(ServerUtilities.PROPERTY_USER_GOOGLEMAIL, "");
    	if (regId.equals("") || user.equals("")) return false; 
    	
        long backoff = ServerUtilities.BACKOFF_MILLI_SECONDS + ServerUtilities.random.nextInt(1000);
    	
    	for (int i=0; i<ServerUtilities.MAX_ATTEMPTS; i++) {
    		Log.d(TAG, "Attempt #" + i + " to check registration status");
    		 
    		try {
		        Map<String,String> params = new HashMap<String,String>();
		        params.put(WebExtClient.KEY_REQUEST, WebExtClient.KEY_MOBILE_IS_REGISTERED); 
		        params.put(ServerUtilities.PROPERTY_REG_ID, regId);
		        params.put(ServerUtilities.PROPERTY_USER_GOOGLEMAIL, email);
		        
	 			String result = ServerUtilities.get(WebExtClient.getInstance(this).getMobileUserRestService(), params);
	 			if (result == null || result.equals("")) return false; 
 			 
 				result = result.replaceAll("\n", "");
 				int regUser = Integer.parseInt(result);
 				return (regUser > 0);
 				
    		 } catch (IOException e) { 
                 Log.e(TAG, "Failed to check registration status on attempt " + i + ":" + e);
                 if (i == ServerUtilities.MAX_ATTEMPTS)  break;  
                 try {
                     Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                     Thread.sleep(backoff);
                 } catch (InterruptedException e1) {
                     // Activity finished before we complete - exit.
                     Log.d(TAG, "Thread interrupted: abort remaining retries!");
                     Thread.currentThread().interrupt();
                     return false;
                 }                 
                 backoff *= 2; // increase backoff exponentially
             
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			 
 		}
    	throw new IOException(); 
    }  

    public boolean autoRegister() {
    	final String userName = prefs.getString(ServerUtilities.PROPERTY_USER_NAME, "");
    	final String regid =  prefs.getString(ServerUtilities.PROPERTY_REG_ID, ""); 
    	if (userName.equals("") || regid.equals("")) return false;
    	
    	return autoRegister(userName, regid);
    }
	
    
    public boolean autoRegister(String userName,  String regid) {
    	
		 if (regid.equals("")) {
			 // GCM registration not finished.
			 Log.d(TAG, "No GCM registration!");
			 return false;
		 } 
		 String gMail = getGoogleAccount();
		 String phoneNo = getPhoneNumber();
         ServerUtilities.register(getApplicationContext(), userName, gMail, phoneNo, regid);
         
         Log.d(TAG, "Registration at web-ext. Registration ID=" +regid+ ", Name="+userName + ", gmail=" +gMail+ ", phone="+phoneNo);
         saveUserPreferences(userName, gMail, phoneNo); 
         return true;
           
     } 
       
    private void saveUserPreferences(String userName, String gMailAccount, String phoneNo) {
    	String regid = prefs.getString(ServerUtilities.PROPERTY_REG_ID, "");
    	
    	// Save the regid for future use (if not already done) - no need to register again.
        SharedPreferences.Editor editor = prefs.edit();
        if (!regid.equals("")) editor.putString(ServerUtilities.PROPERTY_REG_ID, regid);
        
        // also save user name, gmail, phone
        editor.putString(ServerUtilities.PROPERTY_USER_NAME, userName);  
        editor.putString(ServerUtilities.PROPERTY_USER_GOOGLEMAIL, gMailAccount);
        editor.putString(ServerUtilities.PROPERTY_USER_PHONE, phoneNo);

        editor.commit(); 
        Log.d(TAG, "Saved Preferences: RegId=" +regid+ ", Name="+userName + ", gmail=" +gMailAccount+ ", phone="+phoneNo);
        
    }
    
    public String getGoogleAccount() {	
    	AccountManager manager = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
    	Account[] list = manager.getAccounts();
    	for(Account account: list) {
    	    if(account.type.equalsIgnoreCase("com.google"))    	   
    	        return account.name;    	    
    	}
    	return "";
    }
    
	public String getPhoneNumber() {
		 TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); 
		 String result = mTelephonyMgr.getLine1Number();		 
		 return (result == null) ? "" : result;
	}
	  
}