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
public abstract class MLEVerifyResponseMixIn {

    @JsonProperty("bios_mle_exists")        
    public abstract Boolean getBiosMLEExists();

    @JsonProperty("bios_mle_exists")        
    public abstract void setBiosMLEExists(Boolean biosMLEExists);

    @JsonProperty("vmm_mle_exists")        
    public abstract Boolean getVmmMLEExists();

    @JsonProperty("vmm_mle_exists")        
    public abstract void setVmmMLEExists(Boolean vmmMLEExists);

    @JsonProperty("error_flag")    
    public abstract Boolean getErrorFlag();

    @JsonProperty("error_flag")    
    public abstract void setErrorFlag(Boolean errorFlag);

    @JsonProperty("error_message")    
    public abstract String getErrorMessage();

    @JsonProperty("error_message")    
    public abstract void setErrorMessage(String errorMessage);
    
}
