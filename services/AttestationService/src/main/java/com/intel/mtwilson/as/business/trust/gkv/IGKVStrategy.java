package com.intel.mtwilson.as.business.trust.gkv;

import java.util.HashMap;

import com.intel.mountwilson.manifest.data.IManifest;

public interface IGKVStrategy {
	
	HashMap<String,? extends IManifest> getBiosGoodKnownManifest(String mleName, String mleVersion, String oemName );
	HashMap<String,? extends IManifest> getVmmGoodKnownManifest(String mleName, String mleVersion, String osName , String osVersion, Integer hostId);
	

}
