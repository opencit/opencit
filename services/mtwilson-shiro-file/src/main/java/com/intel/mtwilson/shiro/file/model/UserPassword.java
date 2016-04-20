/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.file.model;

import com.intel.mtwilson.crypto.password.HashProtection;


/**
 *  salt should be 8 bytes long minimum recommended in PKCS5 standard
 * 
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  password_hash bytea NOT NULL,
  salt bytea NOT NULL,
  iterations integer DEFAULT 1,
  algorithm character varying(128) NOT NULL,
 *
 * @author jbuhacoff
 */
public class UserPassword implements HashProtection {
    private String username;
    private byte[] passwordHash;
    private byte[] salt;
    private int iterations;
    private String algorithm;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
}
