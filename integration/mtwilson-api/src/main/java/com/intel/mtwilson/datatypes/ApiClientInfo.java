/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author jbuhacoff
 */
public class ApiClientInfo {

    @JsonProperty
    public String name;

    @JsonProperty
    public byte[] certificate;

    @JsonProperty
    public byte[] fingerprint;

    @JsonProperty
    public String issuer;

    @JsonProperty
    public Integer serialNumber;

    @JsonProperty
    public Date expires;

    @JsonProperty
    public boolean enabled;

    @JsonProperty
    public String status;

    @JsonProperty
    public String[] roles;

    @JsonProperty
    public String comment;
    
}
