/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Convenience wrapper around Java's MessageDigest interface.
 * Subclasses of this wrapper must be stateless: the implementation will 
 * digest the complete message and does
 * not support chunks or updates like the MessageDigest instances.
 * Cloning method is not needed due to the stateless property.
 * @author jbuhacoff
 */
public abstract class DigestAlgorithm {
    /*
    private MessageDigest md;
    public DigestAlgorithm(MessageDigest md) {
        this.md = md;
    }
    public DigestAlgorithm(String algorithm) throws NoSuchAlgorithmException {
        this.md = MessageDigest.getInstance(algorithm);
    }*/
    
    /**
     * 
     * @return a string that identifies the algorithm implemented by this instance
     */
    public String getAlgorithm() { return getMessageDigestInstance().getAlgorithm(); }
    
    /**
     * 
     * @return length of the digest in bytes
     */
    public int getDigestLength() { return getMessageDigestInstance().getDigestLength(); }
    
    /**
     * 
     * @param message
     * @return digest of the message using the algorithm returned by getAlgorithm() having the length in bytes returned by getDigestLength()
     */
    public byte[] digest(byte[] message) {
        MessageDigest instance = getMessageDigestInstance();
        byte[] digest = instance.digest(message);
        return digest;
    }
    
    /*
    protected MessageDigest getMessageDigestInstance() {
        return md;
    }
    */
    protected abstract MessageDigest getMessageDigestInstance();
    
    
}
