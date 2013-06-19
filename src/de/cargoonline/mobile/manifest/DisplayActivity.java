package de.cargoonline.mobile.manifest; 
  
import java.util.ArrayList;

import de.cargoonline.mobile.R;
import de.cargoonline.mobile.StartActivity;
import de.cargoonline.mobile.rest.COServiceReceiver;
import de.cargoonline.mobile.uiutils.CommonIntents;
import android.os.Bundle; 
import android.app.Activity;  
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ExpandableListView; 
import android.widget.LinearLayout;
import android.widget.TextView;

public class DisplayActivity extends Activity {

	private ExpandableListView manifestList;
	private ManifestListAdapter adapter;
	private ManifestDataProvider dataProvider; 
	private Button reqButton;
	private boolean submitPossible;
	private COServiceReceiver receiver; 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display); 

        reqButton = (Button) findViewById(R.id.submitButton);
        manifestList = (ExpandableListView) findViewById(R.id.manifest_list);
        TextView tvEori = (TextView) findViewById(R.id.listheader_eori_no);
        TextView getSpeditionName = (TextView) findViewById(R.id.listheader_spedition_name);
        TextView tvManifestId = (TextView) findViewById(R.id.listheader_manifest_id); 
        
        Bundle bundle = getIntent().getExtras();
        dataProvider = ManifestDataProvider.create(bundle);  
       
        adapter = new ManifestListAdapter(
       		this, 
       		dataProvider.getAwbGroups(), 
       		dataProvider.getAwbGroupInfos(), 
       		dataProvider.getMrnPositions()
        );
        manifestList.setAdapter(adapter); 
        
        for (int i=0; i < adapter.getGroupCount(); i++)
        	manifestList.expandGroup(i);
        
        tvEori.setText(dataProvider.getEoriNo());
        getSpeditionName.setText(dataProvider.getSpeditionName()); 
        tvManifestId.setText(dataProvider.getManifestId()); 
        
        reqButton.setBackgroundResource(R.drawable.button_bg_pressed);
        reqButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent arg1) {
				v.setBackgroundResource(R.drawable.button_bg_red);
				return false;
			}        	
        });        	
    	reqButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    	startRequest();
			}
		});
    	 
    	receiver = new COServiceReceiver();
        setManifestViewLayout();
    }
    
    private void startRequest() { 
    	if (submitPossible) {
        	ArrayList<String> mrnsToCommit = adapter.getSelectedPositions();                 	
        	CommonIntents.startSubmitService(this, mrnsToCommit, dataProvider.getSpeditionId(), dataProvider.getManifestId());
        	setWaitingLayout(R.string.commit_in_process);
        } else {
        	CommonIntents.startManifestDataService(this, COServiceReceiver.ACTION_RELOAD);
        	setWaitingLayout(R.string.reloading_data);
        }    	
    } 
    
    private void setWaitingLayout(int msg) {
        LinearLayout waitingLayout = (LinearLayout) findViewById(R.id.manifest_waiting);
        TextView waitingText = (TextView) findViewById(R.id.manifest_waiting_text);
        waitingText.setText(msg);

        manifestList.setVisibility(View.GONE);
        reqButton.setVisibility(View.GONE);
        waitingLayout.setVisibility(View.VISIBLE); 
    }
    
    public void setManifestViewLayout() {
        LinearLayout waitingLayout = (LinearLayout) findViewById(R.id.manifest_waiting);

        waitingLayout.setVisibility(View.GONE);
        manifestList.setVisibility(View.VISIBLE);
        reqButton.setVisibility(View.VISIBLE); 
        checkButtonState();
    }
     
    public void checkButtonState() {
    	submitPossible = (adapter == null) ? false : (adapter.getSelectedPositions().size() > 0);
    	
    	if (submitPossible) {
    		reqButton.setText(R.string.gestellen);
    	} else {
    		reqButton.setText(R.string.check_state);
    	}
        reqButton.setBackgroundResource(R.drawable.button_bg_pressed);
    }
    
    
    public boolean isSubmittable() {
    	return submitPossible;
    }
    
    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
    	// listen to action "mrns submitted"
        IntentFilter submitfilter = new IntentFilter(COServiceReceiver.ACTION_SUBMIT);
        submitfilter.addCategory(Intent.CATEGORY_DEFAULT); 
        registerReceiver(receiver, submitfilter);  

        // also listen to "manifest reload" actions  
	    IntentFilter manifestfilter = new IntentFilter(COServiceReceiver.ACTION_RELOAD);
        manifestfilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, manifestfilter);
        super.onResume();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

        	Intent i = new Intent(this, StartActivity.class);
        	startActivity(i); 
            return true;   
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	dataProvider.reset();
        finish();
    }   
}