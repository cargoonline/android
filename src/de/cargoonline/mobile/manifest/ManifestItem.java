package de.cargoonline.mobile.manifest; 

import java.util.ArrayList;

import de.cargoonline.mobile.R;

public class ManifestItem {
	public enum ManifestItemType { MRN, AWB }
	
	public static final int AWB_NUMBER_LENGTH = 8; 	
	public static final int AWB_PREFIX_LENGTH = 4; 
	public static final char AWB_DELIMITER = '-'; 
	
	private String awbNumber; 
	private String flightNumber;
	private String flightLocation; 
	
	public ManifestItem(String awb, String flightno, String flightloc) {
		awbNumber = formatAwb(awb); 
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
	
	/*
	 * AWB format has to be "xxx-yyyyyyyy" 
	 * -> force y = 8! (fill leading zeroes)
	 */
	private String formatAwb(String input) {
		String[] parts = input.split(String.valueOf(AWB_DELIMITER));
		if (parts.length < 2) throw new NumberFormatException("wrong awb format! has to be [prefix]-[number]");		
		
		int leadingZeroCount = AWB_NUMBER_LENGTH - parts[1].length();
		StringBuilder sb = new StringBuilder();
		sb.append(parts[0]);
		sb.append(AWB_DELIMITER);
		for (int i=1; i<leadingZeroCount; i++) sb.append(0);
		sb.append(parts[1]);
		
		return sb.toString();		
	}
	

	public String getManifestId() {
		return ManifestDataProvider.create(null).getManifestId();
	}
	
	public String getSpeditionId() {
		return ManifestDataProvider.create(null).getSpeditionId();
	}  
	
	public static boolean containsFreeMRN(ArrayList<ManifestItem> mrns) {    	
    	for (ManifestItem mrn : mrns) {  
    		ManifestMRNPosition cur = (ManifestMRNPosition) mrn; 
    		int status = cur.getStatus();
    		if (status <= ManifestMRNPosition.MAX_FREE_STATE) 
    			return true;
    	}
    	return false;
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
