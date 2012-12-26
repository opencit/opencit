package com.intel.mountwilson.manifest.data;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.logging.Logger;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.manifest.helper.SHA1HashBuilder;
import com.intel.mtwilson.datatypes.ErrorCode;
import java.util.ArrayList;
import java.util.List;

/**
 * XXX the interface needs to change, see comments on IManifest
 */
public class PcrModuleManifest extends PcrManifest implements IManifest {
	Logger log = Logger.getLogger(getClass().getName());
	
	private SHA1HashBuilder builder = new SHA1HashBuilder();
	
	public PcrModuleManifest(int pcrNumber, String pcrValue) {
		super(pcrNumber, pcrValue);

	}

	private HashMap<String, ModuleManifest> moduleManifests = new HashMap<String, ModuleManifest>();
        
        private List<ModuleManifest> untrustedModules = new ArrayList<ModuleManifest>();

        public List<ModuleManifest> getUntrustedModules() {
            return untrustedModules;
        }

        /**
         * XXX currently this code allows a host to have a SUBSET of the modules defined in the whitelist and still be trusted... 
         * @param goodKnownValue
         * @return 
         */
	@Override
	public boolean verify(IManifest goodKnownValue) {
		boolean isTrusted = true;
		
		if( goodKnownValue instanceof PcrModuleManifest && (((PcrManifest) goodKnownValue).getPcrValue() == null
				|| ((PcrManifest) goodKnownValue).getPcrValue().isEmpty() )){
			HashMap<String, ModuleManifest> gkvModuleManifests = ((PcrModuleManifest) goodKnownValue)
					.getModuleManifests();

			// verify modules
			if(gkvModuleManifests.size() <= 0)
				throw new ASException(ErrorCode.AS_MISSING_PCR_MANIFEST, getPcrNumber());

			for (ModuleManifest gkvModuleManifest : gkvModuleManifests.values()) {
				
                            // XXX if the whitelist host has 2 related modules, a v0 and v1, and the host being checked has only the second one, it will be named v0 ... so even if it has the right value it will be marked incorrect!!
				if (moduleManifests.containsKey(gkvModuleManifest.getMFKey())) {
                                        ModuleManifest moduleManifest = moduleManifests.get(gkvModuleManifest.getMFKey());
					boolean trustStatus = moduleManifest.verify(
							gkvModuleManifest);
					if (!trustStatus) {
						log.info(String.format("Manifest %s-%s did not match. Set trusted = false ",
								gkvModuleManifest.getComponentName(),
								gkvModuleManifest.getPackageName()));
						isTrusted = false;
                                                moduleManifest.setWhiteListValue(gkvModuleManifest.getDigestValue());
						getUntrustedModules().add(moduleManifest);
                                                //Removed break here so that we get all the mismatched modules instead of the first failed one.
					}
				} 
                                // XXX TODO  -- need a getMissingModules()  list?  otherwise the web interface incorrectly populates the missing module as being present...
//				else {
//					log.info(String.format(
//							"Manifest %s-%s not found in the host manifest. Set trusted = false and stop processing other modules.",
//							gkvModuleManifest.getComponentName(),
//							gkvModuleManifest.getPackageName()));
//					isTrusted = false;
//					break;
//					
//				}
			}
			
			
			log.info(String.format("PCR [%s] Digest value [%s] - Computed digest [%s]",String.valueOf(getPcrNumber()),getPcrValue(),getComputedDigest()));
			if(!getPcrValue().equalsIgnoreCase(getComputedDigest()) && isTrusted){
				log.info(String.format("PCR [%s] Digest value  and Computed digest does not match",getPcrValue()));
				isTrusted = false;
			}
			
		}else{
			if( !super.verify(goodKnownValue))
				isTrusted = false;
		}
			

		
		verifyStatus = isTrusted;
		
		log.info( String.format("Returing verify status [%s] for pcr [%d]", String.valueOf( verifyStatus),getPcrNumber()));
		
		return isTrusted;
	}

	public HashMap<String, ModuleManifest> getModuleManifests() {
		return moduleManifests;
	}

	public void setModuleManifests(
			HashMap<String, ModuleManifest> moduleManifests) {
		this.moduleManifests = moduleManifests;
	}
	
	public void appendDigest(byte[] dataToAppend){
		
		builder.append(dataToAppend);
	}


	
	public String getComputedDigest(){
		
		String digest = byteArrayToHexString(builder.get_data());
		return digest;
	}

	protected String byteArrayToHexString(byte[] bytes) {
		BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}

	

}
