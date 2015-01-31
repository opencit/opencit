/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.key2;

/**
 * This really refers to a MAC or HMAC key since a regular hash function doesn't
 * need a key.
 * @author jbuhacoff
 */
public class DigestKey extends DigestKeyAttributes {
    private byte[] digestKeyId;
    private byte[] encoded;
    
    /**
     * The key id can be used to look up the key in a database or 
     * key server when the encoded key is not present
     */
    public byte[] getDigestKeyId() {
        return digestKeyId;
    }

    public void setDigestKeyId(byte[] digestKeyId) {
        this.digestKeyId = digestKeyId;
    }


    
    /**
     * The encoded key, in the format specified by its attributes
     */

    
    public byte[] getEncoded() {
        return encoded;
    }

    public void setEncoded(byte[] encoded) {
        this.encoded = encoded;
    }


    
    @Override
    public DigestKey copy() {
        DigestKey newInstance = new DigestKey();
        newInstance.copyFrom(this);
//        copy.digestKeyId = this.digestKeyId;
//        copy.encoded = this.encoded;
        return newInstance;
    }
    
    public void copyFrom(DigestKey source) {
        super.copyFrom(source);
        this.digestKeyId = source.digestKeyId;
        this.encoded = source.encoded;
    }

}
