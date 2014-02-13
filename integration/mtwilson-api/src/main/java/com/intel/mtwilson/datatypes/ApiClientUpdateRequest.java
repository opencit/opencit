/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author jbuhacoff
 */
public class ApiClientUpdateRequest {

    /**
     * The fingerprint is to IDENTIFY the record to update. You cannot
     * use this field to change the fingerprint of an existing record.
     */
    @JsonProperty
    public byte[] fingerprint;

    @JsonProperty
    public boolean enabled;

    @JsonProperty
    public String status;

    @JsonProperty
    public String[] roles;

    @JsonProperty
    public String comment;
    
}
