/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.policy.Fault;
import java.util.Set;

/**
 *
 * @author jbuhacoff
 */
public class PcrEventLogMissingExpectedEntries extends Fault {
    private PcrIndex pcrIndex;
    private Set<Measurement> missingEntries;
    public PcrEventLogMissingExpectedEntries(PcrIndex pcrIndex, Set<Measurement> missingEntries) {
        super("Module manifest for PCR %d missing %d expected entries", pcrIndex.toInteger(), missingEntries.size());
        this.pcrIndex = pcrIndex;
        this.missingEntries = missingEntries;
    }
    
    public PcrIndex getPcrIndex() { return pcrIndex; }
    public Set<Measurement> getMissingEntries() { return missingEntries; }
}
