package de.cargoonline.mobile.manifest;

import java.util.ArrayList;

import de.cargoonline.mobile.R;
import de.cargoonline.mobile.uiutils.AlertDialogManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;  
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
    	Integer statusImg = mrnItem.getStatusImg();
    	if (statusImg != null) { // MRN already committed, has state
    		iv.setImageResource(statusImg);
    		cb.setVisibility(View.GONE);
    		iv.setVisibility(View.VISIBLE); 
    		convertView.setOnClickListener(new OnClickListener() {  
	            public void onClick(View v) { 
	            	showMRNdetails(v);
	            }  
	        }); 
    	} else {  
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
    		iv.setVisibility(View.GONE); 
    	}  
		activity.checkButtonState();
        return convertView;
    }
   
    
    public void showMRNdetails(View v) {   
        CheckBox cb = (CheckBox) v.findViewById(R.id.mrn_cb);
        if (cb.getVisibility() == View.VISIBLE) return; // no details for free MRNs
          
        ManifestMRNPosition mrnItem = getMRNFromView(v); 
        if (mrnItem == null) return;

    	Integer mrnDetails = mrnItem.getStatusDetails();
    	String mrnTitle = mrnItem.getMrnNumber();
    	Integer mrnAlertIcon = mrnItem.getAlertSymbol();
    	
    	if (mrnDetails == null || mrnTitle == null || mrnAlertIcon == null) return;
    	
    	Context c = v.getContext();
    	alert.showAlertDialog(
    			c.getString(R.string.mrn_details),
    			c.getString(R.string.mrn_no)+"\n"+mrnTitle + "\n\n"
    			+ c.getString(R.string.state)+"\n"+c.getString(mrnDetails), 
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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,  ViewGroup parent) {
        ManifestItem group = getGroupInfos(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_layout, null);
        }
        ((TextView) convertView.findViewById(R.id.tv_awb)).setText(group.getAwbNumber());
        ((TextView) convertView.findViewById(R.id.tv_flightno)).setText(group.getFlightNumber());
        ((TextView) convertView.findViewById(R.id.tv_flightloc)).setText(group.getFlightLocation());
        
        ArrayList<ManifestItem> mrnsForCurrentAwb = mrnPositions.get(groupPosition);
        int stateDependentBackgroundResource = ManifestItem.getMostCriticalStateResource(mrnsForCurrentAwb);
        
        convertView.setBackgroundResource(stateDependentBackgroundResource);
        return convertView;
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
  
}