package de.cargoonline.mobile.uiutils;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent; 
import de.cargoonline.mobile.GetManifestDataActivity;
import de.cargoonline.mobile.R;
import de.cargoonline.mobile.camera.QRScanActivity;
import de.cargoonline.mobile.manifest.ManifestMRNPosition;
import de.cargoonline.mobile.rest.RegistrationService;
import de.cargoonline.mobile.rest.ServerUtilities;
import de.cargoonline.mobile.rest.WebExtClient;

public class CommonIntents {
	   public static final String PREF_STORE = "CargoOnline";
	   
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
	    
	    public static void checkRegistration(Context c, String userName, String regid) {
	    	Intent registerIntent = new Intent(c, RegistrationService.class);
			registerIntent.putExtra(ServerUtilities.PROPERTY_USER_NAME, userName);
			registerIntent.putExtra(ServerUtilities.PROPERTY_REG_ID, regid); 
			c.startService(registerIntent); 
	    }
}
