/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.keystore;

import com.intel.dcsg.cpg.crypto.key.password.Password;

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
    
    public SinglePasswordKeyProtectionDelegate(Password password) {
        this.password = password.toCharArray();
    }
    
    @Override
    public char[] getPassword(String keyId) {
        return password;
    }
    
}
