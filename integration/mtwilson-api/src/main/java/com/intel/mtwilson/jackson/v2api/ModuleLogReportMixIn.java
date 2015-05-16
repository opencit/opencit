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
public abstract class ModuleLogReportMixIn {

    @JsonProperty("component_name")
    public abstract String getComponentName();

    @JsonProperty("component_name")
    public abstract void setComponentName(String componentName);

    @JsonProperty("value")
    public abstract String getValue();

    @JsonProperty("value")
    public abstract void setValue(String value);
    
    @JsonProperty("whitelist_value")
    public abstract String getWhitelistValue();

    @JsonProperty("whitelist_value")
    public abstract void setWhitelistValue(String whitelistValue);
        
    @JsonProperty("trust_status")
    public abstract Integer getTrustStatus();

    @JsonProperty("trust_status")
    public abstract void setTrustStatus(Integer trustStatus);
        
}
