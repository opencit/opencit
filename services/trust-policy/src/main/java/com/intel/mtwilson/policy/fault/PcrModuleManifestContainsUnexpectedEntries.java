/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.validation.Fault;
import java.util.Set;

/**
 *
 * @author jbuhacoff
 */
public class PcrModuleManifestContainsUnexpectedEntries extends Fault {
    private PcrIndex pcrIndex;
    private Set<Measurement> unexpectedEntries;
    public PcrModuleManifestContainsUnexpectedEntries(PcrIndex pcrIndex, Set<Measurement> unexpectedEntries) {
        super("Module manifest for PCR %d contains %d unexpected entries", pcrIndex.toInteger(), unexpectedEntries.size());
        this.pcrIndex = pcrIndex;
        this.unexpectedEntries = unexpectedEntries;
    }
    
    public PcrIndex getPcrIndex() { return pcrIndex; }
    public Set<Measurement> getUnexpectedEntries() { return unexpectedEntries; }
}
