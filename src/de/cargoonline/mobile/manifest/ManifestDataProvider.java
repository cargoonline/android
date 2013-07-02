package de.cargoonline.mobile.manifest;

import java.util.ArrayList;
 
import de.cargoonline.mobile.rest.ManifestDataService;
import de.cargoonline.mobile.rest.WebExtClient;

import android.os.Bundle;

public class ManifestDataProvider {
	
	private static ManifestDataProvider instance;
	private ArrayList<ArrayList<ManifestItem>> mrnPositions; 
	private ArrayList<ManifestItem> awbGroupInfos; 
	private ArrayList<String> awbGroups;
	private String eoriNo; 
	private String manifestId;
	private String speditionId;
	private String speditionName;
	
    private ManifestDataProvider() {}
    
    public static ManifestDataProvider create(Bundle bundle) {
    	if (instance == null) {
    		instance = new ManifestDataProvider();
    	}  	
    	return (bundle == null) ? instance : instance.createManifestData(bundle);
    }
    
    private ManifestDataProvider createManifestData(Bundle bundle) {
    	if (bundle == null) return null;

    	manifestId = bundle.getString(WebExtClient.KEY_MANIFEST_ID);
    	speditionName = bundle.getString(ManifestDataService.KEY_SPEDITION_NAME);
    	speditionId = bundle.getString(WebExtClient.KEY_SPEDITION_ID);
    	eoriNo = bundle.getStringArray(ManifestDataService.KEY_EORI_NO[0])[0];
    	       
    	String[] mrnNumbers = bundle.getStringArray(ManifestDataService.KEY_MRN_NO);
        String[] awbNumbers = bundle.getStringArray(ManifestDataService.KEY_AWB_NO[1]);
        String[] flightNumbers = bundle.getStringArray(ManifestDataService.KEY_FLIGHT_NO);
        String[] flightLocations = bundle.getStringArray(ManifestDataService.KEY_FLIGHT_LOCATION);
        String[] status = bundle.getStringArray(ManifestDataService.KEY_STATUS);
        String[] detailTxts = bundle.getStringArray(ManifestDataService.KEY_DETAIL_TEXT);

     	awbGroups = new ArrayList<String>();
        awbGroupInfos = new ArrayList<ManifestItem>();
        mrnPositions = new ArrayList<ArrayList<ManifestItem>>();
        
        for (int i=0; i < mrnNumbers.length; i++) {
        	
        	ManifestItem newMRN = new ManifestMRNPosition(awbNumbers[i], status[i], mrnNumbers[i], detailTxts[i]);
        	
        	if (!awbGroups.contains(awbNumbers[i])) {
        		awbGroups.add(awbNumbers[i]);
        		awbGroupInfos.add(new ManifestItem(awbNumbers[i], flightNumbers[i], flightLocations[i]));
        		
        		ArrayList<ManifestItem> newGroupList = new ArrayList<ManifestItem>();
        		newGroupList.add(newMRN);
        		mrnPositions.add(newGroupList); 
        	} else { 
        		for (int j=0; j<awbGroups.size(); j++) {
        			if (awbGroups.get(j).equals(awbNumbers[i])) {
        				mrnPositions.get(j).add(newMRN);
        			}
        		}
        	}  
        }  
        return instance;    	
    }
    
	public ArrayList<ArrayList<ManifestItem>> getMrnPositions() {
		return mrnPositions;
	}
	public ArrayList<ManifestItem> getAwbGroupInfos() {
		return awbGroupInfos;
	}
	public ArrayList<String> getAwbGroups() {
		return awbGroups;
	} 
	public String getEoriNo() {
		return eoriNo;
	} 
	public String getManifestId() {
		return manifestId;
	} 
	public String getSpeditionId() {
		return speditionId;
	}
	public String getSpeditionName() {
		return speditionName;
	}
	
	public void reset() {
		manifestId = null;
    	speditionName = null;
    	speditionId = null;
    	eoriNo = null;
     	awbGroups = null;
        awbGroupInfos = null;
        mrnPositions = null;
	}
}
