/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.x509;

import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jbuhacoff
 */
public class Fingerprint {
    private byte[] bytes;
    
    public Fingerprint(byte[] bytes) {
        this.bytes = bytes;
    }
    
    public byte[] getBytes() {
        return bytes;
    }
    
    public String getHex() {
        return Hex.encodeHexString(bytes);
    }
}
