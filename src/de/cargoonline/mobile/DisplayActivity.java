package de.cargoonline.mobile; 
  
import java.util.ArrayList;

import de.cargoonline.mobile.manifest.ManifestDataProvider; 
import de.cargoonline.mobile.manifest.ManifestListAdapter; 
import de.cargoonline.mobile.manifest.ManifestMRNPosition;
import de.cargoonline.mobile.rest.WebExtClient; 
import android.os.AsyncTask;
import android.os.Bundle; 
import android.app.Activity;  
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ExpandableListView; 
import android.widget.TextView;
import android.widget.Toast;

public class DisplayActivity extends Activity {

	private ExpandableListView manifestList;
	private ManifestListAdapter adapter;
	private ManifestDataProvider dataProvider; 
	private Button reqButton;
	private boolean submitPossible;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display); 
        
       Bundle bundle = getIntent().getExtras();
       dataProvider = ManifestDataProvider.create(bundle);  
       
       adapter = new ManifestListAdapter(
       		this, 
       		dataProvider.getAwbGroups(), 
       		dataProvider.getAwbGroupInfos(), 
       		dataProvider.getMrnPositions()
       );

        manifestList = (ExpandableListView) findViewById(R.id.manifest_list);
        manifestList.setAdapter(adapter); 
        
        for (int i=0; i < adapter.getGroupCount(); i++)
        	manifestList.expandGroup(i);
        
        TextView tvEori = (TextView) findViewById(R.id.listheader_eori_no);
        tvEori.setText(dataProvider.getEoriNo());
        TextView getSpeditionName = (TextView) findViewById(R.id.listheader_spedition_name);
        getSpeditionName.setText(dataProvider.getSpeditionName()); 
        TextView tvManifestId = (TextView) findViewById(R.id.listheader_manifest_id);
        tvManifestId.setText(dataProvider.getManifestId()); 
        
        reqButton = (Button) findViewById(R.id.showLastManifestButton);
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
    }
    
    private void startRequest() { 
    	
        new AsyncTask<Void,Void,String>() {
            @Override
            protected String doInBackground(Void... params) {
            	Context c = getApplicationContext();
            	
                WebExtClient webExtclient = WebExtClient.getInstance(c);
                checkButtonState();
                
                if (submitPossible) {
                	ArrayList<ManifestMRNPosition> mrnsToCommit = adapter.getSelectedPositions(); 
                	webExtclient.submitMRNPositions(c, mrnsToCommit, dataProvider.getSpeditionId()); 
                } else {
                	// TODO status aktualisieren
                }
               
            	return "xxx"; // TODO                
            }

            @Override
            protected void onPostExecute(String msg) {
            	switchLayoutAfterCommit(); 
            }
        }.execute(null, null, null);
    	   
    }
    
    private void switchLayoutAfterCommit() {
     	Toast.makeText(this, R.string.commit_in_process, Toast.LENGTH_SHORT).show();
     	checkButtonState();
    }
     
    public void checkButtonState() {
    	submitPossible = (adapter.getSelectedPositions().size() > 0);
    	
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