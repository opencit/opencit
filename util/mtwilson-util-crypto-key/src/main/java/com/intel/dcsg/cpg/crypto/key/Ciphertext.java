/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

/**
 * When decrypting, the Ciphertext object represents UNTRUSTED INPUT.
 * 
 * @author jbuhacoff
 */
public class Ciphertext {
    protected byte[] keyId;
    protected byte[] iv;
    protected byte[] message;
    protected Protection protection;

    public byte[] getIv() {
        return iv;
    }

    public byte[] getKeyId() {
        return keyId;
    }

    public byte[] getMessage() {
        return message;
    }

    public Protection getProtection() {
        return protection;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public void setKeyId(byte[] keyId) {
        this.keyId = keyId;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public void setProtection(Protection protection) {
        this.protection = protection;
    }

    
}
