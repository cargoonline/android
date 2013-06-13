package de.cargoonline.mobile.rest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map; 

import org.json.JSONArray; 

import de.cargoonline.mobile.manifest.ManifestMRNPosition;
import de.cargoonline.mobile.uiutils.CommonIntents;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;
import android.util.Log;

public class WebExtClient {

	public static final String TAG = "CO WebExtClient";

	public static final String DEFAULT_HOST = "cargoonline3.dbh.de"; // TODO production sys = cargoonline.dbh.de
	public static final String PROTOCOL = "http";
	public static final String SERVICE_ROOT_DIR = "/web_ext/";
	public static final String MOBILE_DIR = "mobile/";
	public static final String MANIFEST_SERVICE = "mobile_manifest.php"; 
 
	public static final String KEY_MANIFEST_ID = "manifestID";
	public static final String KEY_SPEDITION_ID = "speditionID";
	public static final String KEY_MANIFEST_PWD = "manifestPwd";
	public static final String KEY_HOSTNAME = "hostname";
	public static final String KEY_NO_CONNECTION = "noConnection";
	
	public static final String KEY_REQUEST = "REQUEST";
	public static final String KEY_REQUEST_SEARCH = "MOBILE_SEARCH";
	public static final String KEY_MOBILE_IS_REGISTERED = "MOBILE_IS_REGISTERED";
	public static final String KEY_REQUEST_REGISTER = "MOBILE_REGISTER";
	public static final String KEY_REQUEST_UNREGISTER = "MOBILE_UNREGISTER";
	public static final String KEY_REQUEST_SUBMIT = "MOBILE_SUBMIT";
	private SharedPreferences prefs;
	private String lastHostname;
	
	private static WebExtClient instance; 
		
	private WebExtClient(Context c) { 
		prefs = c.getSharedPreferences(CommonIntents.PREF_STORE, Context.MODE_PRIVATE);
		lastHostname = prefs.getString(KEY_HOSTNAME, DEFAULT_HOST);
	} 
	 
	
	public static WebExtClient getInstance(Context c) { 
		if (instance == null) return new WebExtClient(c);
		
        return instance;
    }
	
	public boolean submitMRNPositions(Context c, ArrayList<ManifestMRNPosition> mrns, String speditionId) {
		
		if (mrns == null || mrns.size() == 0) return false; // no mrns to submit, continue.
	
		String regId = prefs.getString(ServerUtilities.PROPERTY_REG_ID, "");
		String user = prefs.getString(ServerUtilities.PROPERTY_USER_NAME, "");	 
		
		// let's build params and trigger post request		
		HashMap<String,String> params = new HashMap<String,String>();
			
		// if registered: send username & gcm reg id to enable push notifications
		if (!regId.equals("") && !user.equals("")) {
			params.put(ServerUtilities.PROPERTY_REG_ID, regId);
			params.put(ServerUtilities.PROPERTY_USER_NAME, user);
		}
		// in any case: send mrn numbers
		JSONArray positions = new JSONArray();
		for (ManifestMRNPosition mrn : mrns) positions.put(mrn.getMrnNumber());				
		params.put("positions", positions.toString());
		
		// furthermore: spedition id ("zoll_nr"), submission time and request key for service
		params.put(KEY_SPEDITION_ID, speditionId);
		params.put(KEY_REQUEST, KEY_REQUEST_SUBMIT);
		params.put("time", Long.toString(System.currentTimeMillis()));

        long backoff = ServerUtilities.BACKOFF_MILLI_SECONDS + ServerUtilities.random.nextInt(1000);
        
		for (int i=0; i < ServerUtilities.MAX_ATTEMPTS; i++) { 
			
			try {
				ServerUtilities.post(getMobileUserRestService(), params);
			} catch (IOException e) {
	            // should retry only on unrecoverable errors (like HTTP error code 503).
	            Log.e(TAG, "Failed to load manifest on attempt " + i + ":" + e);
	            if (i == ServerUtilities.MAX_ATTEMPTS) {
	                break;
	            }
	            try {
	                Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
	                Thread.sleep(backoff);
	            } catch (InterruptedException e1) {
	                // Activity finished before we complete - exit.
	                Log.d(TAG, "Thread interrupted: abort remaining retries!");
	                Thread.currentThread().interrupt();
	                return false;
	            }
	            // increase backoff exponentially
	            backoff *= 2;
	        } 
		}
		return true;
	}
	 
	 
    public String getManifestData(String speditionId, String manifestId, String manifestPwd) throws IllegalArgumentException {
    	
		if (manifestId == null || manifestId.equals(""))
			throw new IllegalArgumentException("No valid manifest id!");
		if (speditionId == null || speditionId.equals(""))
			throw new IllegalArgumentException("No valid spedition id!");
		if (manifestPwd == null || manifestPwd.equals(""))
			throw new IllegalArgumentException("No valid manifest password!");
		
         
        Map<String,String> params = new HashMap<String,String>();
        params.put(KEY_REQUEST, KEY_REQUEST_SEARCH);
        params.put(KEY_MANIFEST_ID, manifestId);
        params.put(KEY_SPEDITION_ID, speditionId);
        params.put(KEY_MANIFEST_PWD, manifestPwd);
         
        long backoff = ServerUtilities.BACKOFF_MILLI_SECONDS + ServerUtilities.random.nextInt(1000);
        
		for (int i=0; i < ServerUtilities.MAX_ATTEMPTS; i++) { 
			
			try {
				return ServerUtilities.get(getManifestRestService(), params);
			} catch (IOException e) {
	            // should retry only on unrecoverable errors (like HTTP error code 503).
	            Log.e(TAG, "Failed to load manifest on attempt " + i + ":" + e);
	            if (i == ServerUtilities.MAX_ATTEMPTS) {
	                break;
	            }
	            try {
	                Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
	                Thread.sleep(backoff);
	            } catch (InterruptedException e1) {
	                // Activity finished before we complete - exit.
	                Log.d(TAG, "Thread interrupted: abort remaining retries!");
	                Thread.currentThread().interrupt();
	                return null;
	            }
	            // increase backoff exponentially
	            backoff *= 2;
	        } 
		} 
        return null;
    } 
    
    public boolean saveLastHostname(String newHost) { 
    	if (newHost == null || newHost.equals("")) return false;
    	
    	lastHostname = newHost;
    	Editor e = prefs.edit();
    	e.putString(KEY_HOSTNAME, newHost);
    	e.commit();
    	return true;
    }
    
    public String getLastHostname() {
    	return lastHostname;
    }
    
    public String getServerRoot() {
    	return PROTOCOL + "://" + getLastHostname() + SERVICE_ROOT_DIR;
    }
	 
    public String getManifestRestService() {
    	return getServerRoot()+MANIFEST_SERVICE;
    }
    	 
    public String getMobileUserRestService() {
    	return getServerRoot()+MOBILE_DIR;
    } 
    
    public static String getGoogleAccount(Context c) {		
    	AccountManager manager = (AccountManager) c.getSystemService(Context.ACCOUNT_SERVICE);
    	Account[] list = manager.getAccounts();
    	for(Account account: list) {
    	    if(account.type.equalsIgnoreCase("com.google"))    	   
    	        return account.name;    	    
    	}
    	return "";
    }
    
	public static String getPhoneNumber(Context c) {
		 TelephonyManager mTelephonyMgr = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE); 
		 String result = mTelephonyMgr.getLine1Number();		 
		 return (result == null) ? "" : result;
	}
	  
}