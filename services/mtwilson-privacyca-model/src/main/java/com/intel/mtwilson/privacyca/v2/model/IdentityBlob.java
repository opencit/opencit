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

    public void setIdentityBlob(byte[] identityBlob) {
        this.identityBlob = identityBlob;
    }

    public byte[] getIdentityBlob() {
        return identityBlob;
    }

    
}
