package de.cargoonline.mobile.manifest; 

import java.util.HashMap;
 
import de.cargoonline.mobile.R;  
 
@SuppressWarnings("serial")
public class ManifestMRNPosition extends ManifestItem {
	
	public final static int FREE_STATE = -1;
	public final static int CLOSED_STATE = 36;
	public final static int MAX_FREE_STATE = 10;
	public final static int MIN_STATE = 36;
	
	public final static HashMap<Integer,Integer> GREEN_STATES = new HashMap<Integer,Integer>() {
		{	     
			put(24, R.string.state24);   
			put(26, R.string.state26);  
		}
     };  
 	public final static HashMap<Integer,Integer> YELLOW_STATES = new HashMap<Integer,Integer>() {
		{	     
			put(11, R.string.state11);   
			put(12, R.string.state12);   
			put(13, R.string.state13);   
			put(14, R.string.state14);   
			put(21, R.string.state21);  
			put(22, R.string.state22);  
			put(23, R.string.state23);  
			put(31, R.string.state31);  
			put(32, R.string.state32);  
			put(33, R.string.state33);  
		}
     };  
 	public final static HashMap<Integer,Integer> RED_STATES = new HashMap<Integer,Integer>() {
		{	     
			put(25, R.string.state25);   
		}
     };  
 	  
     
	private int status;
	private String mrnNumber;  
	
	public ManifestMRNPosition(String awbNumber, String state, String mrn)  { 
		super(awbNumber);
		try {
			status = Integer.parseInt(state);
			if (status < MAX_FREE_STATE) status = FREE_STATE;
		} catch (Exception e) {
			status = FREE_STATE;
		}
		mrnNumber = mrn; 
	}
	
	public int getStatus() {
		return status;
	} 

	@Override
	public ManifestItemType getType() {
		return ManifestItemType.MRN;	
	}
		
	public Integer getAlertSymbol() {
		if (GREEN_STATES.containsKey(status) || status >= CLOSED_STATE) return R.drawable.success;
		if (YELLOW_STATES.containsKey(status)) return R.drawable.pending;
		if (RED_STATES.containsKey(status)) return R.drawable.fail; 
		if (status > MAX_FREE_STATE) return  R.drawable.fail; // unknown state
		return null; // free MRN 
	}
	
	public Integer getStatusImg() {
		if (GREEN_STATES.containsKey(status) || status >= CLOSED_STATE) return R.drawable.green;
		if (YELLOW_STATES.containsKey(status)) return R.drawable.yellow;
		if (RED_STATES.containsKey(status)) return R.drawable.red; 
		if (status > MAX_FREE_STATE) return  R.drawable.red; // unknown state				
		return null; // free MRN 
	} 
	
	public Integer getStatusDetails() {
		if (GREEN_STATES.containsKey(status) || status >= CLOSED_STATE) return GREEN_STATES.get(status);
		if (YELLOW_STATES.containsKey(status)) return YELLOW_STATES.get(status);
		if (RED_STATES.containsKey(status)) return RED_STATES.get(status); // TODO: fehler infos!
		
		if (status > MAX_FREE_STATE) return 0; // unknown state
		// TODO: fehler infos!
					
		return null; // free MRN 
	} 
	
	public String getMrnNumber() {
		return mrnNumber;
	}  
}
