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
public class XmlMeasurementValueMismatch extends Fault {
    private Sha1Digest expectedValue;
    private Sha1Digest actualValue;
    
    public XmlMeasurementValueMismatch() { } // for desearializing jackson
    
    public XmlMeasurementValueMismatch(Sha1Digest expectedValue, Sha1Digest actualValue) {
        super("Host XML measurement log final hash with value %s does not match expected value %s", actualValue.toString(), expectedValue.toString());
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
    }
    
    public Sha1Digest getExpectedValue() { return expectedValue; }
    public Sha1Digest getActualValue() { return actualValue; }
}
