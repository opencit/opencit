/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * The identity blob is the TCG-specified aik cert wrapped with the tpm's 
 * endorsement public key 
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="identity_blob")
public class IdentityBlob {
    private byte[] identityBlob;
    private int symSize;
    private int asymSize;

    public int getSymSize() {
        return symSize;
    }

    public void setSymSize(int symSize) {
        this.symSize = symSize;
    }

    public int getAsymSize() {
        return asymSize;
    }

    public void setAsymSize(int asymSize) {
        this.asymSize = asymSize;
    }

    public void setIdentityBlob(byte[] identityBlob) {
        this.identityBlob = identityBlob;
    }

    public byte[] getIdentityBlob() {
        return identityBlob;
    }    
}
