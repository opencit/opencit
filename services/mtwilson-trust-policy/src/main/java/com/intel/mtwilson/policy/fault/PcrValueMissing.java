/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.policy.Fault;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class PcrValueMissing extends Fault {
    private PcrIndex missingPcrIndex;
    
    public PcrValueMissing() { } // for desearializing jackson
        
    public PcrValueMissing(PcrIndex missingPcrIndex) {
        super("Host report does not include required PCR %d", missingPcrIndex.toInteger());
        this.missingPcrIndex = missingPcrIndex;
    }
    public PcrIndex getPcrIndex() { return missingPcrIndex; }
}
