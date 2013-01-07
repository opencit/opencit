package com.intel.mtwilson.as.business.trust.gkv;

import com.intel.mountwilson.manifest.data.IManifest;
import java.util.HashMap;

// BUG #497 need to replace HashMap<String, ? extends IManifest> with new PcrManifest  model object,  and possibly make new methods for ModuleManifest
public interface IGKVStrategy {
	
	HashMap<String,? extends IManifest> getBiosGoodKnownManifest(String mleName, String mleVersion, String oemName );
	HashMap<String,? extends IManifest> getVmmGoodKnownManifest(String mleName, String mleVersion, String osName , String osVersion, Integer hostId);
	

}
