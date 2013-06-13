package de.cargoonline.mobile.uiutils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface; 
 
public class AlertDialogManager {
	
	private AlertDialog alertDialog;
	 
	public AlertDialogManager() {}
	
	public AlertDialogManager(Context c) {
		setContext(c);
	}
	 
    /**
     * Function to display simple Alert Dialog
     * @param context - application context
     * @param title - alert dialog title
     * @param message - alert message
     * @param status - success/failure (used to set icon)
     *               - pass null if you don't want icon
     * */
    public void showAlertDialog(String title, String message, Integer statusImg ) { 
 
        // Setting Dialog Title
        alertDialog.setTitle(title);
         
        // Setting Dialog Message
        alertDialog.setMessage(message);
 

        // Setting alert dialog icon 
        if (statusImg != null)
        	alertDialog.setIcon(alertDialog.getContext().getResources().getDrawable(statusImg));        
     
        
 
        // Showing Alert Message
        alertDialog.show();
    }
    
    public void onOk(DialogInterface.OnClickListener listener) {
    	alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", listener);
    }
    
    public void setContext(Context c) { 
		alertDialog = new AlertDialog.Builder(c).create();
    }
}