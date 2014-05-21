/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto.password;

/**
 * Similar to PasswordProtection in cpg-crypto-key but includes only the
 * hash algorithm name, salt, and number of iterations.  
 * This HashProtection class is specifically for hashing raw data (passwords)
 * whereas the PasswordProtection class in cpg-crypto-key is for symmetric
 * encryption using a password-based key derivation, so it also includes
 * ciphers, key derivation algorithm, etc. which are not needed here.
 * 
 * @author jbuhacoff
 */
public interface HashProtection {
    //byte[] getPasswordHash();
    byte[] getSalt();
    int getIterations();
    String getAlgorithm();
}
