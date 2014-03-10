/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="identity_challenge_request")
public class IdentityChallengeRequest {
    private byte[] identityRequest;
    private byte[] endorsementCertificate;

    public void setIdentityRequest(byte[] identityRequest) {
        this.identityRequest = identityRequest;
    }

    public void setEndorsementCertificate(byte[] endorsementCertificate) {
        this.endorsementCertificate = endorsementCertificate;
    }

    public byte[] getIdentityRequest() {
        return identityRequest;
    }

    public byte[] getEndorsementCertificate() {
        return endorsementCertificate;
    }
    
}
