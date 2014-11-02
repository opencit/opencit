/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.crypto.key.RandomSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jbuhacoff
 */
public class RandomUtil {
    private final static RandomSource rnd = new RandomSource();
    
    public static byte[] randomByteArray(int size) {
        return rnd.nextBytes(size);
    }

    public static String randomHexString(int bytes) {
        return Hex.encodeHexString(randomByteArray(bytes));
    }
    
    public static String randomBase64String(int bytes) {
        return Base64.encodeBase64String(randomByteArray(bytes));
    }
    
}
