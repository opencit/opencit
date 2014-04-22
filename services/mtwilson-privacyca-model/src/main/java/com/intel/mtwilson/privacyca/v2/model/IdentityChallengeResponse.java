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
@JacksonXmlRootElement(localName="identity_challenge_response")
public class IdentityChallengeResponse {
    private byte[] identityRequestResponseToChallenge;

    public void setChallengeResponse(byte[] identityRequestResponseToChallenge) {
        this.identityRequestResponseToChallenge = identityRequestResponseToChallenge;
    }

    public byte[] getChallengeResponse() {
        return identityRequestResponseToChallenge;
    }

    
}
