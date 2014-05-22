/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.password;

import com.intel.mtwilson.crypto.password.HashProtection;

/**
 * 
 * @author jbuhacoff
 */
public class HashedPassword implements HashProtection {
    private byte[] passwordHash;
    private byte[] salt;
    private int iterations;
    private String algorithm;
    
    public byte[] getPasswordHash() {
        return passwordHash;
    }
   
    @Override
    public byte[] getSalt() {
        return salt;
    }

    @Override
    public int getIterations() {
        return iterations;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }


}

