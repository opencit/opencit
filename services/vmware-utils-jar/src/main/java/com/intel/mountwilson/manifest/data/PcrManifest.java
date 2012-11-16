package com.intel.mountwilson.manifest.data;

import java.util.logging.Logger;


public class PcrManifest implements IManifest {
	Logger log = Logger.getLogger(getClass().getName());
	public PcrManifest(int pcrNumber, String pcrValue) {
		super();
		this.pcrNumber = pcrNumber;
		this.pcrValue = pcrValue;
	}

	private int pcrNumber;
	private String pcrValue;
	protected boolean verifyStatus = false;
	
	
	public int getPcrNumber() {
		return pcrNumber;
	}

	public void setPcrNumber(int pcrNumber) {
		this.pcrNumber = pcrNumber;
	}

	public String getPcrValue() {
		return pcrValue;
	}

	public void setPcrValue(String pcrValue) {
		this.pcrValue = pcrValue;
	}

	@Override
	public boolean verify(IManifest gkv) {

		PcrManifest goodKnownValue = (PcrManifest) gkv;
		
		log.info(String.format("GKV PCR [%d] value [%s] and manifest pcr value [%s]",getPcrNumber(),goodKnownValue.getPcrValue(),getPcrValue() ));
		
//		// TODO: need to check what to do with this case
//		if(goodKnownValue.getPcrValue() == null || goodKnownValue.getPcrValue().isEmpty()){
//			verifyStatus = true;
//		}
		
		
		if ( goodKnownValue.getPcrValue().equalsIgnoreCase(this.pcrValue)) {
			verifyStatus = true;
		}
		return verifyStatus;
	}

	public boolean getVerifyStatus() {
		return verifyStatus;
	}

}
