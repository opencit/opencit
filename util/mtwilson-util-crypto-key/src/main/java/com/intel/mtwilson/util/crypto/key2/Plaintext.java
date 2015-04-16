/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.key2;

/**
 *
 * @author jbuhacoff
 */
public class Plaintext {
    private byte[] message;

    public Plaintext(byte[] message) {
        this.message = message;
    }

    public byte[] getMessage() {
        return message;
    }
    
    
}
