/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.policy.Fault;
import java.util.Set;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class VMMeasurementLogValueMismatchEntries extends Fault {
    private Set<Measurement> mismatchEntries;
    
    public VMMeasurementLogValueMismatchEntries() { } // for desearializing jackson
    
    public VMMeasurementLogValueMismatchEntries(Set<Measurement> mismatchEntries) {
        super("VM measurement log contains %d entries for which the values are modified.", mismatchEntries.size());
        this.mismatchEntries = mismatchEntries;
    }
    
    public Set<Measurement> getMissingEntries() { return mismatchEntries; }
}
