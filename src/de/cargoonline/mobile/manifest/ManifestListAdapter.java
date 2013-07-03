package de.cargoonline.mobile.manifest;

import java.util.ArrayList;

import de.cargoonline.mobile.R;
import de.cargoonline.mobile.rest.ManifestDataService;
import de.cargoonline.mobile.rest.WebExtClient;
import de.cargoonline.mobile.uiutils.AlertDialogManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;  
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener; 
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseExpandableListAdapter;

public class ManifestListAdapter extends BaseExpandableListAdapter  {
	
	private static final String TAG = "CO ManifestListAdapter";
	private Context context; 
	int layoutResourceId;    
	private ArrayList<ArrayList<ManifestItem>> mrnPositions;
	private ArrayList<ManifestItem> awbGroupInfos;
	private ArrayList<String> awbGroups;
    private AlertDialogManager alert;
    private ArrayList<String> selectedFreeMrns;
    private DisplayActivity activity;

	public ManifestListAdapter(Context c, ArrayList<String> groups, ArrayList<ManifestItem> groupInfos, ArrayList<ArrayList<ManifestItem>> positions) {
		context = c;
		activity = (DisplayActivity) c;
		awbGroups = groups;
		awbGroupInfos = groupInfos;
		mrnPositions = positions; 
		alert = new AlertDialogManager(c);
		selectedFreeMrns = new ArrayList<String>();
	}
	
    @Override
    public boolean areAllItemsEnabled()   {
        return true;
    } 
    
    @Override
    public Object getChild(int groupPosition, int childPosition) {
    	  return mrnPositions.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
     
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        ManifestMRNPosition mrnItem = (ManifestMRNPosition) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.child_layout, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.tvMrn);
        tv.setText(mrnItem.getMrnNumber());  
       
        ImageView iv = (ImageView) convertView.findViewById(R.id.mrn_status);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.mrn_cb);
    	String detailInfoFromServer = mrnItem.getMrnDetailsFromServer(); 
    	boolean hasDetails = detailInfoFromServer != null && !detailInfoFromServer.equals("");
    	Integer statusImg = hasDetails ? R.drawable.red : mrnItem.getStatusImg();
    	 
    	if (statusImg > 0) { // MRN already committed, has state
    		iv.setImageResource(statusImg); 
    		iv.setVisibility(View.VISIBLE); 
    		convertView.setOnClickListener(new OnClickListener() {  
	            public void onClick(View v) { 
	            	showMRNdetails(v);
	            }  
	        }); 
    	} else {  
    		iv.setVisibility(View.GONE); 
    	}  
    	
    	if (mrnItem.getStatus() > 0) 
     		cb.setVisibility(View.INVISIBLE);
    	else { 
    		selectedFreeMrns.add(mrnItem.getMrnNumber()); 
    		cb.setVisibility(View.VISIBLE);
    		cb.setTag(mrnItem);
    		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() { 
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				 try {
					 ManifestMRNPosition mrnItem = (ManifestMRNPosition) buttonView.getTag();
					 if (isChecked) selectedFreeMrns.add(mrnItem.getMrnNumber());
					 else selectedFreeMrns.remove(mrnItem.getMrnNumber());  
					 activity.checkButtonState();
				 } catch (Exception e) {
					 Log.e(TAG, "error: unable to check free mrn!");
				 }
				
			}}); 
    	}
		activity.checkButtonState();
        return convertView;
    }
   
    
    public void showMRNdetails(View v) {   
        ImageView iv = (ImageView) v.findViewById(R.id.mrn_status);
        if (iv.getVisibility() == View.GONE) return; // no details for free MRNs
          
        ManifestMRNPosition mrnItem = getMRNFromView(v); 
        if (mrnItem == null) return;

    	Context c = v.getContext();
    	
        String mrnDetails = mrnItem.getMrnDetailsFromServer();
        if (mrnDetails == null || mrnDetails.equals("")) 
        	mrnDetails = c.getString(mrnItem.getDefaultStatusDetails());
        
    	String mrnTitle = mrnItem.getMrnNumber();
    	Integer mrnAlertIcon = mrnItem.getAlertSymbol();
    	
    	if (mrnDetails == null || mrnTitle == null) return;
    	
    	alert.showAlertDialog(
    			mrnTitle,
    			mrnDetails, 
    			mrnAlertIcon); 
    }
    
    private ManifestMRNPosition getMRNFromView(View v) {
    	TextView tv = (TextView) v.findViewById(R.id.tvMrn);
        String mrnNo = tv.getText().toString();

        for (ArrayList<ManifestItem> mrnsPerAwb : mrnPositions) {
        	for (ManifestItem mrn : mrnsPerAwb) { 

        		ManifestMRNPosition cur = (ManifestMRNPosition) mrn;  
            	if (cur.getMrnNumber().equals(mrnNo)) {
            		return cur;
            	}
        	}
        } 
        return null;
    }

    
    @Override
    public int getChildrenCount(int groupPosition) {
        return mrnPositions.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return awbGroups.get(groupPosition);
    }
    
    public ManifestItem getGroupInfos(int groupPosition) {
        return awbGroupInfos.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return awbGroups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView,  ViewGroup parent) {
        final ManifestItem group = getGroupInfos(groupPosition);
   
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_layout, null);
        }
        ((TextView) convertView.findViewById(R.id.tv_awb)).setText(group.getAwbNumber());
        ((TextView) convertView.findViewById(R.id.tv_flightno)).setText(group.getFlightNumber());
        ((TextView) convertView.findViewById(R.id.tv_flightloc)).setText(group.getFlightLocation());
        ImageButton editFlightButton = (ImageButton) convertView.findViewById(R.id.editFlightButton);
        
        ArrayList<ManifestItem> mrns = mrnPositions.get(groupPosition);
        editFlightButton.setVisibility(View.VISIBLE);
        if (ManifestItem.containsFreeMRN(mrns)) {
        	
	        editFlightButton.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent arg1) {
					v.setBackgroundResource(R.drawable.button_bg_red);
					return false;
				}        	
	        }); 
	        editFlightButton.setOnClickListener(new OnClickListener() { 
	        	@Override
				public void onClick(View v) { 
	        		startEditFlightActivity(v.getContext(), groupPosition, group);				
	        		v.setBackgroundResource(R.drawable.button_bg_pressed);	
				} 
	        });
        } else {
            editFlightButton.setVisibility(View.INVISIBLE);
        }        
        
        convertView.setBackgroundResource(
        		ManifestItem.getMostCriticalStateResource(mrns)
        );
        convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) { 
				activity.toggleExpanded(groupPosition);
			}});
        
        return convertView;
    }
    
    public void setExpanded(int pos) {
    	activity.toggleExpanded(pos);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    } 
    
    public ArrayList<String> getSelectedPositions() {
    	return selectedFreeMrns;
    } 
    
    public void startEditFlightActivity(Context c, int position, ManifestItem group) {
    	Intent i = new Intent(c, EditFlightActivity.class);
		i.putExtra(ManifestDataService.KEY_AWB_NO[1], group.getAwbNumber());
		i.putExtra(ManifestDataService.KEY_AWB_POSITION, position);		
		i.putExtra(ManifestDataService.KEY_FLIGHT_NO, group.getFlightNumber());
		i.putExtra(ManifestDataService.KEY_FLIGHT_LOCATION, group.getFlightLocation());
		i.putExtra(WebExtClient.KEY_MANIFEST_ID, group.getManifestId());
		i.putExtra(WebExtClient.KEY_SPEDITION_ID, group.getSpeditionId());
		c.startActivity(i); 
    }
  
}