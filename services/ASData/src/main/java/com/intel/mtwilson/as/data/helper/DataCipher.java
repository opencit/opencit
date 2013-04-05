/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.data.helper;

/**
 *
 * @author jbuhacoff
 */
public interface DataCipher {
    String encryptString(String plaintext);
    String decryptString(String ciphertext);
}
