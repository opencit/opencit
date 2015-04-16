/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.model.PcrIndex;
//import com.intel.mtwilson.model.Sha1Digest;
import com.intel.mtwilson.policy.Fault;
import com.intel.dcsg.cpg.crypto.Sha1Digest;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class PcrValueMismatch extends Fault {
    private PcrIndex pcrIndex;
    private Sha1Digest expectedValue;
    private Sha1Digest actualValue;
    
    public PcrValueMismatch() { } // for desearializing jackson
    
    public PcrValueMismatch(PcrIndex pcrIndex, Sha1Digest expectedValue, Sha1Digest actualValue) {
        super("Host PCR %d with value %s does not match expected value %s", pcrIndex.toInteger(), actualValue.toString(), expectedValue.toString());
        this.pcrIndex = pcrIndex;
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
    }
    
    public PcrIndex getPcrIndex() { return pcrIndex; }
    public Sha1Digest getExpectedValue() { return expectedValue; }
    public Sha1Digest getActualValue() { return actualValue; }
}
