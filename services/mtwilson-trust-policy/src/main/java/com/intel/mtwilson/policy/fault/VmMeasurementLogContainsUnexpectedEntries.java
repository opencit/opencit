/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.model.VmMeasurement;
import com.intel.mtwilson.policy.Fault;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class VmMeasurementLogContainsUnexpectedEntries extends Fault {
    private List<VmMeasurement> unexpectedEntries;
    
    public VmMeasurementLogContainsUnexpectedEntries() {
        unexpectedEntries = new ArrayList<VmMeasurement>() {};
    } // for desearializing jackson
    
    public VmMeasurementLogContainsUnexpectedEntries(List<VmMeasurement> unexpectedEntries) {
        super("VM measurement log contains %d unexpected entries", unexpectedEntries.size());
        this.unexpectedEntries = unexpectedEntries;
    }
    
    public List<VmMeasurement> getUnexpectedEntries() { return unexpectedEntries; }
}
