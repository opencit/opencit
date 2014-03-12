/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.x509;

/**
 *
 * @author jbuhacoff
 */
public class Credential {
    private byte[] signature;
    private byte[] digest;

    public Credential(byte[] signature, byte[] digest) {
        this.signature = signature;
        this.digest = digest;
    }
    
    /**
     * which represents the document and selected headers as specified in the Authorization header value
     * @return 
     */
    public byte[] getDigest() {
        return digest;
    }

    /**
     * by the private key over the digest
     * @return 
     */
    public byte[] getSignature() {
        return signature;
    }
    
    
}
