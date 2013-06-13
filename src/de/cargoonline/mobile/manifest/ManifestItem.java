package de.cargoonline.mobile.manifest; 

import java.util.ArrayList;

import de.cargoonline.mobile.R;

public class ManifestItem {
	public enum ManifestItemType { MRN, AWB }
	
	private String awbNumber; 
	private String flightNumber;
	private String flightLocation; 
	
	public ManifestItem(String awb, String flightno, String flightloc) {
		awbNumber = awb; 
		flightNumber = (flightno.equals("null")) ? "" :  flightno;
		flightLocation = (flightloc.equals("null")) ? "" : flightloc; 
	}
	   
	public ManifestItem(String awb) {
		awbNumber = awb;
	}
	  
	public ManifestItemType getType() {
		return ManifestItemType.AWB;	
	}
	
	public String getAwbNumber() {
		return awbNumber;
	}  
	   
	public void setAwbNumber(String awb) {
		awbNumber = awb;
	}  
	 
	public String getFlightNumber() {
		return flightNumber;
	} 
	public String getFlightLocation() {
		return flightLocation;
	} 
	
	public String getFlightLocationShort() {
		return flightLocation.split(" -")[0];
	} 
	
	public static int getMostCriticalStateResource(ArrayList<ManifestItem> mrns) {
	    	boolean hasYellowState = false;
	    	boolean hasGreenState = false;
	    	
	    	for (ManifestItem mrn : mrns) {  
	    		ManifestMRNPosition cur = (ManifestMRNPosition) mrn; 
	    		int status = cur.getStatus();
	    		
	    		if (ManifestMRNPosition.RED_STATES.containsKey(status))
	    			return R.drawable.semitransparent_group_red;
	    		
	    		if (ManifestMRNPosition.YELLOW_STATES.containsKey(status))
	    			hasYellowState = true;
	    		else if (ManifestMRNPosition.GREEN_STATES.containsKey(status))
	    			hasGreenState = true;
	        }
	    	
	    	return 	(hasYellowState ? 	R.drawable.semitransparent_group_yellow : 
	    			(hasGreenState ? 	R.drawable.semitransparent_group_green : 
	    								R.drawable.semitransparent_group_bg));
	    }
}
