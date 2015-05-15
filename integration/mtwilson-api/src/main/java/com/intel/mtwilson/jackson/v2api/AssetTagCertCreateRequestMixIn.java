/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author jbuhacoff
 */
public abstract class AssetTagCertCreateRequestMixIn {

    @JsonProperty("X509_certificate")
    public abstract byte[] getCertificate();

    @JsonProperty("X509_certificate")
    public abstract void setCertificate(byte[] credential);
    
}
