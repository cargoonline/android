package de.cargoonline.mobile.manifest;

import de.cargoonline.mobile.MainMenuActivity;
import de.cargoonline.mobile.R; 
import de.cargoonline.mobile.rest.COServiceReceiver;
import de.cargoonline.mobile.rest.EditFlightService;
import de.cargoonline.mobile.rest.ManifestDataService;
import de.cargoonline.mobile.rest.WebExtClient;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle; 
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditFlightActivity extends MainMenuActivity {

	private EditText locationEdit;
	private EditText numberEdit;
	private int awbPosition;
	private TextView awbText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editflight);
		
		Bundle bundle = getIntent().getExtras();
		final String awbNo = bundle.getString(ManifestDataService.KEY_AWB_NO[1]);
		final String manifestId = bundle.getString(WebExtClient.KEY_MANIFEST_ID);
		final String speditionId = bundle.getString(WebExtClient.KEY_SPEDITION_ID);
		awbPosition = bundle.getInt(ManifestDataService.KEY_AWB_POSITION);
		String flightNo = bundle.getString(ManifestDataService.KEY_FLIGHT_NO);
		String flightLocation = bundle.getString(ManifestDataService.KEY_FLIGHT_LOCATION);

		awbText = (TextView) this.findViewById(R.id.tv_editflight_awb);		
		locationEdit = (EditText) this.findViewById(R.id.et_editflight_location);
		numberEdit = (EditText) this.findViewById(R.id.et_editflight_no);
		
		awbText.setText(awbNo);
		locationEdit.setText(flightLocation);
		numberEdit.setText(flightNo); 

		Button submitBtn = (Button) this.findViewById(R.id.editFlightSubmitButton);
		submitBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent arg1) {
				v.setBackgroundResource(R.drawable.button_bg_red);
				return false;
			}        	
        }); 
		submitBtn.setOnClickListener(new OnClickListener() { 
        	@Override
			public void onClick(View v) {
        		startEditFlightService(awbNo, manifestId, speditionId);
        		v.setBackgroundResource(R.drawable.button_bg_pressed);
        		
        		Intent i = new Intent(v.getContext(), DisplayActivity.class);
        		i.putExtra(WebExtClient.KEY_REQUEST_EDIT_FLIGHT, true);
            	startActivity(i);
			} 
        });		
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	Intent i = new Intent(this, DisplayActivity.class);
        	startActivity(i);
            return true;   
        }
        return super.onKeyDown(keyCode, event);
    }
	
	public void startEditFlightService(String awbNo, String manifestId, String speditionId) {
		Intent i = new Intent(this, EditFlightService.class);
		i.putExtra(ManifestDataService.KEY_FLIGHT_LOCATION, locationEdit.getText().toString());
		i.putExtra(ManifestDataService.KEY_FLIGHT_NO, numberEdit.getText().toString());
		i.putExtra(ManifestDataService.KEY_AWB_POSITION, awbPosition);
		i.putExtra(ManifestDataService.KEY_AWB_NO[1], awbNo);		
		i.putExtra(WebExtClient.KEY_MANIFEST_ID, manifestId);
		i.putExtra(WebExtClient.KEY_SPEDITION_ID, speditionId);
		startService(i); 
	} 
}