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
public class CaInfo {

    @JsonProperty
    public boolean enabled;

    @JsonProperty
    public String issuer;

    @JsonProperty
    public byte[] fingerprint;

    @JsonProperty
    public Date expires;

    @JsonProperty
    public String comment;
    
}
