package com.intel.mountwilson.manifest;

import java.util.HashMap;

import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mountwilson.manifest.data.IManifest;

public interface IManifestStrategy {
	
	HashMap<String,? extends IManifest> getManifest(TblHosts host) throws Exception;
}
