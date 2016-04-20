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
import java.util.List;
import java.util.Set;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class PcrEventLogContainsUnexpectedEntries extends Fault {
    private PcrIndex pcrIndex;
    private List<Measurement> unexpectedEntries;
    
    public PcrEventLogContainsUnexpectedEntries() { } // for desearializing jackson
    
    public PcrEventLogContainsUnexpectedEntries(PcrIndex pcrIndex, List<Measurement> unexpectedEntries) {
        super("Module manifest for PCR %d contains %d unexpected entries", pcrIndex.toInteger(), unexpectedEntries.size());
        this.pcrIndex = pcrIndex;
        this.unexpectedEntries = unexpectedEntries;
    }
    
    public PcrIndex getPcrIndex() { return pcrIndex; }
    public List<Measurement> getUnexpectedEntries() { return unexpectedEntries; }
}
