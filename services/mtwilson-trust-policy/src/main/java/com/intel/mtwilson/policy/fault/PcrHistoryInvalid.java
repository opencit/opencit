/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.policy.Fault;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class PcrHistoryInvalid extends Fault {
    public PcrHistoryInvalid() { } // for desearializing jackson
    
    public PcrHistoryInvalid(Pcr expected) {
        super("PCR History for PCR %d does not match its expected value %s", expected.getIndex().toInteger(), expected.getValue().toString());
    }
}
