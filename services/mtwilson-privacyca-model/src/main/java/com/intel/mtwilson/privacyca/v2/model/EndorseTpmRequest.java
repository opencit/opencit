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
@JacksonXmlRootElement(localName="endorse_tpm_request")
public class EndorseTpmRequest {
    private byte[] ekModulus;

    public void setEkModulus(byte[] ekModulus) {
        this.ekModulus = ekModulus;
    }

    public byte[] getEkModulus() {
        return ekModulus;
    }    
}
