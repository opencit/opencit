/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.key2;

import com.intel.dcsg.cpg.io.Copyable;

/**
 * Describes the protection given to some data, including encryption and
 * integrity attributes and optionally the specific key id's used for
 * encryption or integrity and optionally the keys themselves.
 * 
 * @author jbuhacoff
 */
public class Protection implements Copyable {
    private CipherKeyAttributes encryption;
    private IntegrityKeyAttributes integrity;

    private Protection() {
    }

    
    public Protection(CipherKeyAttributes encryption, IntegrityKeyAttributes integrity) {
        this.encryption = encryption;
        this.integrity = integrity;
    }

    public CipherKeyAttributes getEncryption() {
        return encryption;
    }

    public IntegrityKeyAttributes getIntegrity() {
        return integrity;
    }

    
    @Override
    public Protection copy() {
        Protection newInstance = new Protection();
        newInstance.copyFrom(this);
        return newInstance;
    }
    
    public void copyFrom(Protection source) {
        this.encryption = source.encryption.copy();
        this.integrity = source.integrity.copy();
    }
}
