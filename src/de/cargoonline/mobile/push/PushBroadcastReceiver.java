package de.cargoonline.mobile.push;
import de.cargoonline.mobile.uiutils.WakeLocker;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PushBroadcastReceiver extends BroadcastReceiver {

    @Override
    public final void onReceive(Context context, Intent intent) {
        PushIntentService.runIntentInService(context, intent);

    	WakeLocker.acquire(context);
    	WakeLocker.release();
        setResult(Activity.RESULT_OK, null, null);
    }
 
    
}
