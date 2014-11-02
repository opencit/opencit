/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import javax.crypto.*;

/**
 * This class supports only 128-bit keys.
 * In version 0.1.4 this class was rewritten to subclass Aes. Please see Aes for all other implementation notes.
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class Aes128 extends Aes {
    public Aes128(byte[] secretKeyAes128) throws CryptographyException {
        super(secretKeyAes128);
        if( secretKeyAes128.length != 16 ) {
            throw new IllegalArgumentException("AES-128 key must be 128 bits");
        }
    }

    public Aes128(SecretKey secretKeyAes128) throws CryptographyException {
        super(secretKeyAes128);
        if( secretKeyAes128.getEncoded().length != 16 ) {
            throw new IllegalArgumentException("AES-128 key must be 128 bits");            
        }
    }
    
    /**
     * TODO: if we deprecate Aes128 then the Aes class could have an instance method generateKey() which creates
     * a key of the length configured in that instance.
     * 
     * @return
     * @throws CryptographyException 
     */
    public static SecretKey generateKey() throws CryptographyException {
        return Aes.generateKey(128);
    }    
}
