/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.i18n.ErrorCode;

/**
 *
 * @author jbuhacoff
 */
public abstract class HostConfigResponseMixIn {

    @JsonProperty("error_message")
    public abstract String getErrorMessage();

    @JsonProperty("error_message")
    public abstract void setErrorMessage(String errorMessage);

    @JsonProperty("host_name")
    public abstract String getHostName();

    @JsonProperty("host_name")
    public abstract void setHostName(String hostName);

    @JsonProperty("status")    
    public abstract String getStatus();

    @JsonProperty("status")        
    public abstract void setStatus(String status);

   @JsonProperty("error_code")        
    public abstract ErrorCode getErrorCode();

   @JsonProperty("error_code")        
    public abstract void setErrorCode(ErrorCode errorCode);
}
