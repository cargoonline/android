package de.cargoonline.mobile.rest;   
import de.cargoonline.mobile.MainMenuActivity;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager; 

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
	public static final String KEY_INPUT_ERROR = "inputError";
	public static final String KEY_WAITING = "waiting";
	
	public static final String KEY_REQUEST = "REQUEST";
	public static final String KEY_RESPONSE = "RESPONSE";
	public static final String KEY_REQUEST_SEARCH = "MOBILE_SEARCH";
	public static final String KEY_MOBILE_IS_REGISTERED = "MOBILE_IS_REGISTERED";
	public static final String KEY_REQUEST_REGISTER = "MOBILE_REGISTER";
	public static final String KEY_REQUEST_UNREGISTER = "MOBILE_UNREGISTER";
	public static final String KEY_REQUEST_SUBMIT = "MOBILE_SUBMIT";
	public static final String KEY_REQUEST_EDIT_FLIGHT = "MOBILE_EDIT_FLIGHT";
	private SharedPreferences prefs;
	private String lastHostname;
	
	private static WebExtClient instance; 
		
	private WebExtClient(Context c) { 
		prefs = c.getSharedPreferences(MainMenuActivity.PREF_STORE, Context.MODE_PRIVATE);
		lastHostname = prefs.getString(KEY_HOSTNAME, DEFAULT_HOST);
	} 
	 
	
	public static WebExtClient getInstance(Context c) { 
		return (instance == null) ? new WebExtClient(c) : instance;  
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