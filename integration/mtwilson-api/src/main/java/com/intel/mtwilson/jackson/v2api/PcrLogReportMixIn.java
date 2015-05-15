/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.datatypes.ModuleLogReport;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public abstract class PcrLogReportMixIn {

    @JsonProperty("name")
    public abstract Integer getName();

    @JsonProperty("name")
    public abstract void setName(Integer name);

    @JsonProperty("trust_status")
    public abstract Integer getTrustStatus();

    @JsonProperty("trust_status")
    public abstract void setTrustStatus(Integer trustStatus);

    @JsonProperty("value")
    public abstract String getValue();

    @JsonProperty("value")
    public abstract void setValue(String value);

    @JsonProperty("verified_on")
    public abstract Date getVerifiedOn();

    @JsonProperty("verified_on")
    public abstract void setVerifiedOn(Date verifiedOn);

    @JsonProperty("module_logs")
    public abstract List<ModuleLogReport> getModuleLogs();

    @JsonProperty("module_logs")
    public abstract void setModuleLogs(List<ModuleLogReport> moduleLogs);

    @JsonProperty("whitelist_value")
    public abstract String getWhiteListValue();
    
    @JsonProperty("whitelist_value")
    public abstract void setWhiteListValue(String whiteListValue);
    
}
