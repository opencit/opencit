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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class XmlMeasurementLogContainsUnexpectedEntries extends Fault {
    private PcrIndex pcrIndex;
    private List<Measurement> unexpectedEntries;
    
    public XmlMeasurementLogContainsUnexpectedEntries() {
        unexpectedEntries = new ArrayList<Measurement>() {};
    } // for desearializing jackson
    
    public XmlMeasurementLogContainsUnexpectedEntries(PcrIndex pcrIndex, List<Measurement> unexpectedEntries) {
        super("XML measurement log for PCR %d contains %d unexpected entries", pcrIndex.toInteger(), unexpectedEntries.size());
        this.pcrIndex = pcrIndex;
        this.unexpectedEntries = unexpectedEntries;
    }
    
    public PcrIndex getPcrIndex() { return pcrIndex; }
    public List<Measurement> getUnexpectedEntries() { return unexpectedEntries; }
}
