/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jbuhacoff
 */
public class Digest {
    private String algorithm; // MD5, SHA-1, SHA-256, SHA-384, SHA-512
    private byte[] data;
    
    public Digest(String algorithm, byte[] data) {
        this.algorithm = algorithm;
        this.data = data;
    }

    /**
     * 
     * @return the digest algorithm name associated with the digest data
     */
    public String getAlgorithm() {
        return algorithm;
    }
    
    /**
     * 
     * @return the digest bytes; modifications to this array will be reflected in the Digest instance
     */
    public byte[] getBytes() { return data; }
    
    /**
     * 
     * @return a copy of the digest bytes
     */
    public byte[] toByteArray() { return Arrays.copyOf(data, data.length); }
    

    public String toHex() {
        return Hex.encodeHexString(data);
    }
    
    public String toBase64() {
        return Base64.encodeBase64String(data);
    }
    
    public Digest extend(byte[] more) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(data);
            md.update(more);
            return new Digest(algorithm,md.digest());
        }
        catch(NoSuchAlgorithmException e) {
            throw new UnsupportedAlgorithmException(algorithm, e);
        }
        
    }
    
}
