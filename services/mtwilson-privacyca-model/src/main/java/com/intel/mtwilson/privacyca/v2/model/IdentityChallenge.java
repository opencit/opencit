/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * The identity challenge is the TCG-specified challenge wrapped with the tpm's 
 * endorsement public key 
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="identity_challenge")
public class IdentityChallenge {
    private byte[] identityChallenge;

    public void setIdentityChallenge(byte[] identityChallenge) {
        this.identityChallenge = identityChallenge;
    }

    public byte[] getIdentityChallenge() {
        return identityChallenge;
    }

    
}
