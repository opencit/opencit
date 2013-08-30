/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.io.Base64Util;
import com.intel.dcsg.cpg.io.HexUtil;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @since 0.1
 * @author jbuhacoff
 */
public enum DigestAlgorithm {
    MD5("MD5", 16),
    SHA1("SHA-1", 20),
    SHA256("SHA-256", 32);
    
    protected final String algorithm;
    protected final int length;
    
    DigestAlgorithm(String algorithmName, int digestLengthBytes) {
        this.algorithm = algorithmName;
        this.length = digestLengthBytes;
    }
    
    public String algorithm() { return algorithm; }
    public int length() { return length; }
    
    public boolean isValid(byte[] value) {
        return value != null && value.length == length;
    }
    
    public boolean isValidHex(String hexValue) {
        if( hexValue == null ) { return false; }
        hexValue = HexUtil.trim(hexValue);
        return (hexValue.length() == length*2) && HexUtil.isHex(hexValue);
    }
    
    public boolean isValidBase64(String base64Value) {
        if( base64Value == null ) { return false; }
        base64Value = Base64Util.trim(base64Value);
        return (base64Value.length() == Math.round(4*Math.ceil(1.0*length/3))) && Base64.isBase64(base64Value);
//        return Base64.isBase64(base64Value);
    }
    
    /**
     * 
     * @param message must not be null
     * @return 
     * @throws UnsupportedOperationException which wraps NoSuchAlgorithmException, thrown if the platform doesn't have an implementation for the selected algoritm
     * @throws NullPointerException or IllegalArgumentException if you pass a null object
     */
    public byte[] digest(byte[] message) {
        try {
            MessageDigest hash = MessageDigest.getInstance(algorithm); // throws NoSuchAlgorithmException; example of algorithm is "MD5", "SHA-1", "SHA-256"
            byte[] digest = hash.digest(message);
            return digest;
        }
        catch(NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("Missing algorithm implementation: "+algorithm, e);
        }
    }
    
}
