package com.intel.mountwilson.manifest.data;

/**
 * XXX this interface must change, it is confusing to use the same interface
 * for both the whitelist value and the actual value. Also, the various
 * implementations of IManifest are not compatible, which makes the interface
 * useless:  PcrManifest can only verify another PcrManifest (it does a cast
 * in the verify() function) and PcrModuleManifest can only verify another
 * PcrModuleManifest. They need to be properly separated, as they are not
 * the same at all. Even a generic interface  Verifier<T> with a function
 * verify(T whitelist) would be better because then you get compile-time 
 * checking on the types, even though they are not compatible with each other,
 * and it's obvious when looking at the source.
 */
public interface IManifest {
	
	 public boolean verify(IManifest goodKnownValue); 

}
