/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.policy.Fault;
import java.util.Set;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class XmlMeasurementLogValueMismatchEntries extends Fault {
    private PcrIndex pcrIndex;
    private Set<Measurement> missingEntries;
    
    public XmlMeasurementLogValueMismatchEntries() { } // for desearializing jackson
    
    public XmlMeasurementLogValueMismatchEntries(PcrIndex pcrIndex, Set<Measurement> missingEntries) {
        super("XML measurement log for PCR %d contains %d entries for which the values are modified.", pcrIndex.toInteger(), missingEntries.size());
        this.pcrIndex = pcrIndex;
        this.missingEntries = missingEntries;
    }
    
    public PcrIndex getPcrIndex() { return pcrIndex; }
    public Set<Measurement> getMissingEntries() { return missingEntries; }
}
