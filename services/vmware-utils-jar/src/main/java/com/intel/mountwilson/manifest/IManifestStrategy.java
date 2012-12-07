package com.intel.mountwilson.manifest;

import java.util.HashMap;

import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mountwilson.manifest.data.IManifest;

/**
 * XXX this interface must change, the "? extends IManifest" works but
 * needlessly complex for what we are doing, especially when you look at
 * the IManifest interface itself.
 */
public interface IManifestStrategy {
	
	HashMap<String,? extends IManifest> getManifest(TblHosts host) throws Exception;
}
