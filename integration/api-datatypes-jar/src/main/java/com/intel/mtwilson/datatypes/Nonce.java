/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

import java.math.BigInteger;

/**
 * XXX TODO this class is just a draft. It needs to be finished.
 * Intent is to be a thin wrapper around byte[] so that we have type safety
 * and readability in our Java code.
 * 
 * If support for base64 and hex is required they should be implemented as
 * Nonce.fromBase64 and Nonce.fromHex because a String constructor would be
 * ambiguous.
 * @author jbuhacoff
 */
public class Nonce {
    byte[] data;
    public Nonce() {
        // TODO: default constructor should create a random number
    }
    public Nonce(byte[] array) {
        data = array;
    }
    public Nonce(BigInteger nonce) {
        
    }
    
    public byte[] toByteArray() { return data; }
}
