/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.model.VmMeasurement;
import com.intel.mtwilson.policy.Fault;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class VmMeasurementLogMissingExpectedEntries extends Fault {
    private Set<VmMeasurement> missingEntries;
    
    public VmMeasurementLogMissingExpectedEntries() {
        missingEntries = new HashSet<>();
    } // for desearializing jackson
    
    public VmMeasurementLogMissingExpectedEntries(Set<VmMeasurement> missingEntries) {
        super("VM measurement log missing %d expected entries", missingEntries.size());
        this.missingEntries = missingEntries;
    }
    
    public Set<VmMeasurement> getMissingEntries() { return missingEntries; }
}
