/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

/**
 *
 * @author jbuhacoff
 */
public class Plaintext {
//    protected byte[] header;
    protected byte[] message;
    protected byte[] digest;
    protected Protection protection;

    public byte[] getMessage() {
        return message;
    }

    public byte[] getDigest() {
        return digest;
    }

    public Protection getProtection() {
        return protection;
    }

    public void setDigest(byte[] digest) {
        this.digest = digest;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public void setProtection(Protection protection) {
        this.protection = protection;
    }
    
    
}
