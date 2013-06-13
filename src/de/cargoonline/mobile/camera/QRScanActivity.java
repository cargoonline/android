 
package de.cargoonline.mobile.camera;

import de.cargoonline.mobile.GetManifestDataActivity;
import de.cargoonline.mobile.R;  
import de.cargoonline.mobile.StartActivity;
import de.cargoonline.mobile.rest.WebExtClient;
import de.cargoonline.mobile.uiutils.AlertDialogManager;  

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;  
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler; 
import android.widget.FrameLayout; 
import android.widget.Toast;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback; 
import android.hardware.Camera.Size; 

/* Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

public class QRScanActivity extends Activity { 
	
    private Camera mCamera;
    private CameraPreview mPreview;
    private boolean previewing = true;
    private Handler autoFocusHandler;   
    private ImageScanner scanner;
    private MediaPlayer shootMP; 
    private AlertDialogManager tryAgainAlert;

    private PreviewCallback previewCb = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);
            
            if (result != 0) {
            	stopCamera();
            	
                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                	String qrData = sym.getData();
                	
                	if (qrData != null) {
                		playShutterSound();
                		String[] parts = qrData.split(";"); // spedition id;manifest id;pwd;[hostname]
                		if (parts.length >= 3) {
                			String host = (parts.length > 3) ? parts[3] : "";
                			releaseCamera();
                            startNextActivity(parts[0], parts[1], parts[2], host);
                		}
                		else {
                			warnInvalidCode(); 
                		}
                	}
                } 
            }
        }  
        
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);  

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        preview.addView(mPreview);         
        
        Toast.makeText(this, R.string.welcome_scan_code, Toast.LENGTH_SHORT).show();
        
        tryAgainAlert = new AlertDialogManager(this);
        tryAgainAlert.onOk(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	resumeCamera();
            }
        });
    }
    

    @Override
    public void onBackPressed() { 
    	releaseCamera();
    	Intent i = new Intent(getApplicationContext(), StartActivity.class);
    	startActivity(i);    
        finish();
        return;
    }
    
    @Override 
    public void onDestroy() {
    	releaseCamera();
    	super.onDestroy();
    }

    public void onPause() {
        super.onPause(); 
        stopCamera();
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        }
        return c;
    }

    public void startNextActivity(String speditionID, String manifestID, String manifestPwd, String host) {
    	
    	// save host as current web ext server
    	WebExtClient webExt = WebExtClient.getInstance(this);
    	webExt.saveLastHostname(host);

    	// try to get manifest - let's see if we are registered on this host.
    	Intent i = new Intent(this, GetManifestDataActivity.class);
		i.putExtra(WebExtClient.KEY_SPEDITION_ID, speditionID);
		i.putExtra(WebExtClient.KEY_MANIFEST_ID, manifestID);
		i.putExtra(WebExtClient.KEY_MANIFEST_PWD, manifestPwd);
		
    	startActivity(i);  
    }
    
    public void warnInvalidCode() {
    	tryAgainAlert.showAlertDialog(
         		getResources().getString(R.string.scan_fail),
         		getResources().getString(R.string.scan_fail_desc), 
         		R.drawable.fail);  
    }
    
    private void releaseCamera() {
        if (mCamera != null) {
        	stopCamera();
            mCamera.release();
            mCamera = null;
        }
    }
    
    private void stopCamera() { 
	    if (mCamera != null) {
	        previewing = false;
	        mCamera.setPreviewCallback(null);
	        mCamera.stopPreview();
	    }
    }

    private void resumeCamera() {
    	previewing = true; 
        mCamera.setPreviewCallback(previewCb);
    	mCamera.startPreview();
    }
    

    private void playShutterSound() {
    	 AudioManager meng = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    	 int volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

	    if (volume != 0) {
			if (shootMP == null)
	            shootMP = MediaPlayer.create(this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
	        
	        if (shootMP != null)
	            shootMP.start();
	    } 
    }
        
    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };  
    
  // Mimic continuous auto-focusing
  private AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
      public void onAutoFocus(boolean success, Camera camera) {
          autoFocusHandler.postDelayed(doAutoFocus, 1000);
      }
  };
}