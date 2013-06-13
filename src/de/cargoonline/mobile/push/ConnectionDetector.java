package de.cargoonline.mobile.push; 

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
  
public class ConnectionDetector {
  
    private Context context;
  
    public ConnectionDetector(Context context){
        this.context = context;
    }
  
    /**
     * Checking for all possible internet providers
     * **/
    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
          if (connectivity != null)
          {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if (info != null)
                  for (int i = 0; i < info.length; i++)
                      if (info[i].getState() == NetworkInfo.State.CONNECTED)
                      {
                          return true;
                      }
  
          }
          return false;
    }
    
	 public boolean hasGoogleAccount() {		 
         AccountManager accountManager = AccountManager.get(context);
         Account[] accounts = accountManager.getAccountsByType("com.google");

         if (accounts.length == 0) 
             return false;
         else 
             return true;
        
	 }

	public boolean isIceCreamSandwichOrAbove() {
         int currentapiVersion = android.os.Build.VERSION.SDK_INT;
         if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                 return true;
         } else {
                 return false;
         }
	}
}