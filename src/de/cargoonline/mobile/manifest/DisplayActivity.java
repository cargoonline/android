package de.cargoonline.mobile.manifest; 
  
import java.util.ArrayList;
import de.cargoonline.mobile.MainMenuActivity;
import de.cargoonline.mobile.R;
import de.cargoonline.mobile.StartActivity;
import de.cargoonline.mobile.rest.COServiceReceiver; 
import de.cargoonline.mobile.rest.WebExtClient;
import android.os.Bundle; 
import android.content.Intent;
import android.content.IntentFilter; 
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;  
import android.widget.ExpandableListView;  
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

public class DisplayActivity extends MainMenuActivity {

	private ManifestDataProvider dataProvider; 
	private ExpandableListView manifestList;
	private ManifestListAdapter adapter;
	private ImageButton reqButton;
	private MenuItem refreshItem;
	private MenuItem scanItem;
	private MenuItem commitItem;	
	private boolean submitPossible;
	private boolean refreshRequired;
	private boolean updateRunning;
	private COServiceReceiver receiver; 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display); 
        Bundle bundle = getIntent().getExtras();

        reqButton = (ImageButton) findViewById(R.id.submitButton);
        manifestList = (ExpandableListView) findViewById(R.id.manifest_list);
        TextView tvEori = (TextView) findViewById(R.id.listheader_eori_no);
        TextView getSpeditionName = (TextView) findViewById(R.id.listheader_spedition_name);
        TextView tvManifestId = (TextView) findViewById(R.id.listheader_manifest_id); 
        refreshRequired = false;
        updateRunning = bundle != null ? bundle.getBoolean(WebExtClient.KEY_REQUEST_EDIT_FLIGHT) : false;
        
        // fill list
        dataProvider = ManifestDataProvider.create(updateRunning ? null : bundle);         
        adapter = new ManifestListAdapter(
       		this, 
       		dataProvider.getAwbGroups(), 
       		dataProvider.getAwbGroupInfos(), 
       		dataProvider.getMrnPositions()
        );
        manifestList.setAdapter(adapter); 
        
        for (int i=0; i < adapter.getGroupCount(); i++) {
        	manifestList.expandGroup(i);   
        }
        
        tvEori.setText(dataProvider.getEoriNo());
        getSpeditionName.setText(dataProvider.getSpeditionName()); 
        tvManifestId.setText(dataProvider.getManifestId()); 
        
        // init refresh / submit button
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
				if (submitPossible) submitPositions();
		    	 else refreshState(); 
			}
		});
    	 
    	// setup broadcast receiver
    	receiver = new COServiceReceiver();
    	
    	// set layout depending on update state is running or not
    	if (updateRunning)
    		setUpdatingFlightLayout();
    	else
    		setManifestViewLayout();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) { 
    	super.onCreateOptionsMenu(menu);  
    	menu.findItem(R.id.menu_manifest).setVisible(false);
    	if (StartActivity.DEBUG_ALLOW_UNREGISTER) 
    		menu.findItem(R.id.menu_unregister).setVisible(false);
    	
        commitItem = menu.add(R.string.gestellen);
        commitItem.setIcon(R.drawable.ic_menu_upload);
        commitItem.setVisible(submitPossible);
        
        refreshItem = menu.add(R.string.check_state); 
        refreshItem.setIcon(R.drawable.ic_menu_refresh);
    	scanItem = menu.add(R.string.welcome_start_scanner);
    	scanItem.setIcon(R.drawable.ic_menu_camera);
    	
        return true;
   }
	    
   @Override
   public boolean onOptionsItemSelected(MenuItem item) { 
	   if (item.equals(refreshItem)) {
		   refreshState();
		   return true;
	   }
	   if (item.equals(commitItem)) {
		   submitPositions();
		   return true;
	   }
	   if (item.equals(scanItem)) {
		  startScanner();
		  return true;
	   }
	  
	   return super.onOptionsItemSelected(item); 
   } 
    
    private void submitPositions() {
    	ArrayList<String> mrnsToCommit = adapter.getSelectedPositions();                 	
    	startSubmitService(mrnsToCommit, dataProvider.getSpeditionId(), dataProvider.getManifestId());
    	setWaitingLayout(R.string.commit_in_process);
    }
    
    public void refreshState() { 
    	startManifestDataService(COServiceReceiver.ACTION_RELOAD);
    	setWaitingLayout(R.string.reloading_data);
    }
    
    private void setWaitingLayout(int msg) {
        FrameLayout waitingLayout = (FrameLayout) findViewById(R.id.manifest_waiting);
        TextView waitingText = (TextView) findViewById(R.id.manifest_waiting_text);
        waitingText.setText(msg);
        reqButton.setEnabled(false);  
        waitingLayout.setVisibility(View.VISIBLE); 
    }
    
    private void setUpdatingFlightLayout() {
        FrameLayout waitingLayout = (FrameLayout) findViewById(R.id.manifest_waiting);
        TextView waitingText = (TextView) findViewById(R.id.manifest_waiting_text);
        waitingText.setText(R.string.updating_flight_data);
        reqButton.setEnabled(false);  
        waitingLayout.setVisibility(View.VISIBLE); 
        checkButtonState();
    }
    
    public void setManifestViewLayout() {
    	FrameLayout waitingLayout = (FrameLayout) findViewById(R.id.manifest_waiting);
    	waitingLayout.setVisibility(View.GONE); 
        reqButton.setEnabled(true);
        checkButtonState();
    }
     
    public void checkButtonState() {
    	submitPossible = (adapter == null) ? false : (adapter.getSelectedPositions().size() > 0);
    	
    	if (submitPossible) {
    		//reqButton.setText(R.string.gestellen);
    		reqButton.setImageResource(R.drawable.ic_menu_upload);
    	} else {
    		//reqButton.setText(R.string.check_state);
    		reqButton.setImageResource(R.drawable.ic_menu_refresh);
    	}
        reqButton.setBackgroundResource(R.drawable.button_bg_pressed);
        if (commitItem != null) commitItem.setVisible(submitPossible);
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
    protected void onStop() {
    	refreshRequired = true;
    	super.onStop();
    }
    
    @Override
    protected void onStart() {
    	if (refreshRequired) refreshState();
    	super.onStart();
    }

    @Override
    protected void onResume() {
    	refreshRequired = false;
    	
    	// listen to action "mrns submitted"
        IntentFilter submitfilter = new IntentFilter(COServiceReceiver.ACTION_SUBMIT);
        submitfilter.addCategory(Intent.CATEGORY_DEFAULT); 
        registerReceiver(receiver, submitfilter);  

        // also listen to "manifest reload" actions  
	    IntentFilter manifestfilter = new IntentFilter(COServiceReceiver.ACTION_RELOAD);
        manifestfilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, manifestfilter); 

        // listen to "flight edited" actions 
 	    IntentFilter editFlightfilter = new IntentFilter(COServiceReceiver.ACTION_EDIT_FLIGHT);
 	    editFlightfilter.addCategory(Intent.CATEGORY_DEFAULT);
         registerReceiver(receiver, editFlightfilter);	  
        
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
    
    public void toggleExpanded(int pos) {
    	if (!manifestList.expandGroup(pos))    	
    		manifestList.collapseGroup(pos);
    }
}
