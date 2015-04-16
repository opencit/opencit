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
public class PcrEventLogInvalid extends Fault {
    public PcrEventLogInvalid() { } // for desearializing jackson
    
    public PcrEventLogInvalid(PcrIndex pcrIndex) {
        super("PCR %d Event Log is invalid", pcrIndex.toInteger());
    }
}
