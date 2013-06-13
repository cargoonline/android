package de.cargoonline.mobile.uiutils;

import android.content.Context;
import android.os.PowerManager;
 

// TODO 
// use window manager flag FLAG_KEEP_SCREEN_ON instead of deprecated methods below
// -> better power management!
public abstract class WakeLocker {
    private static PowerManager.WakeLock wakeLock;
 
    public static void acquire(Context context) {
        if (wakeLock != null) wakeLock.release();
 
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "WakeLock");
        wakeLock.acquire();
    }
 
    public static void release() {
        if (wakeLock != null) wakeLock.release(); wakeLock = null;
    }
}