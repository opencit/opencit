package com.intel.mountwilson.util.vmware.data;

/**
 * XXX this interface needs to be redesigned, it is not effectively 
 * communicating what is going on. 
 */
public interface IManifest {
	
	 public boolean verify(IManifest goodKnownValue); 

}
