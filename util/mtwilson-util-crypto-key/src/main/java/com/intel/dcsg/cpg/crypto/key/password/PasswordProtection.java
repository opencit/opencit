/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key.password;

import com.intel.dcsg.cpg.crypto.key.Protection;

/**
 *
 * @author jbuhacoff
 */
public class PasswordProtection extends Protection {
    protected String keyAlgorithm;
    protected int saltBytes;
    protected int iterations;
    
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public int getSaltBytes() {
        return saltBytes;
    }

    public int getIterations() {
        return iterations;
    }

    @Override
    protected void setAlgorithm(String algorithm) {
        super.setAlgorithm(algorithm);
    }

    @Override
    protected void setBlockSizeBytes(int blockSizeBytes) {
        super.setBlockSizeBytes(blockSizeBytes);
    }

    @Override
    protected void setCipher(String cipher) {
        super.setCipher(cipher);
    }

    @Override
    protected void setDigestAlgorithm(String digestAlgorithm) {
        super.setDigestAlgorithm(digestAlgorithm);
    }

    @Override
    protected void setDigestSizeBytes(int digestSizeBytes) {
        super.setDigestSizeBytes(digestSizeBytes);
    }

    protected void setIterations(int iterations) {
        this.iterations = iterations;
    }

    protected void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    @Override
    protected void setKeyLengthBits(int keyLengthBits) {
        super.setKeyLengthBits(keyLengthBits);
    }

    @Override
    protected void setMode(String mode) {
        super.setMode(mode);
    }

    @Override
    protected void setPadding(String padding) {
        super.setPadding(padding);
    }

    protected void setSaltBytes(int saltBytes) {
        this.saltBytes = saltBytes;
    }
    
    
    /*
    @Override
    public void copy(Object other) {
        super.copy(other);
        if( other instanceof PasswordProtection ) {
            PasswordProtection protection = (PasswordProtection)other;
            this.keyAlgorithm = protection.keyAlgorithm;
            this.saltBytes = protection.saltBytes;
            this.iterations = protection.iterations;
        }
    }
    */
}
