/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * This would contain the trust report and also the actual measurements for user verification.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class VmReport {
    private String vmTrustPolicy;
    private String vmMeasurements;

    public String getVmTrustPolicy() {
        return vmTrustPolicy;
    }

    public void setVmTrustPolicy(String vmTrustPolicy) {
        this.vmTrustPolicy = vmTrustPolicy;
    }

    public String getVmMeasurements() {
        return vmMeasurements;
    }

    public void setVmMeasurements(String vmMeasurements) {
        this.vmMeasurements = vmMeasurements;
    }
    
}
