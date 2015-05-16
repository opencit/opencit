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
public abstract class ApiClientCreateRequestMixIn {

    @JsonProperty("x509_certificate")
    public abstract byte[] getCertificate();
    
    @JsonProperty("x509_certificate")
    public abstract void setCertificate(byte[] credential);

    @JsonProperty("roles")
    public abstract String[] getRoles();
    
    @JsonProperty("roles")
    public abstract void setRoles(String[] roles);
    
}
