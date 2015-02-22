/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.keystore;

/**
 * Used to set the same password for each key entry. This can simplify 
 * keystore management for non-interactive applications but may be less
 * secure than choosing a different password for each key.
 * 
 * @author jbuhacoff
 */
public class SinglePasswordKeyProtectionDelegate implements KeyProtectionDelegate {
    private char[] password;

    public SinglePasswordKeyProtectionDelegate(char[] password) {
        this.password = password;
    }
    
    
    @Override
    public char[] getPassword(String keyId) {
        return password;
    }
    
}
