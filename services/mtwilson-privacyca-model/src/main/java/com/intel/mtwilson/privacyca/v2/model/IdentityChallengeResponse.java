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
    private String tpmVersion;
    private byte[] aikName;

    public byte[] getAikName() {
        return aikName;
    }

    public void setAikName(byte[] aikName) {
        this.aikName = aikName;
    }
    
    public String getTpmVersion() {
        return tpmVersion;
    }

    public void setTpmVersion(String tpmVersion) {
        this.tpmVersion = tpmVersion;
    }

    public void setChallengeResponse(byte[] identityRequestResponseToChallenge) {
        this.identityRequestResponseToChallenge = identityRequestResponseToChallenge;
    }

    public byte[] getChallengeResponse() {
        return identityRequestResponseToChallenge;
    }

    
}
