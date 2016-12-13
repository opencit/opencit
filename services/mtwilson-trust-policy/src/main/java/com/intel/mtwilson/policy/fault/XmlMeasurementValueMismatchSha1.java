/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.dcsg.cpg.crypto.Sha1Digest;

/**
 *
 * @author dczech
 */
public class XmlMeasurementValueMismatchSha1 extends XmlMeasurementValueMismatch<Sha1Digest> {
    public XmlMeasurementValueMismatchSha1(Sha1Digest expectedValue, Sha1Digest actualValue) {
        super(expectedValue, actualValue);
    }
    
    @JsonCreator
    public XmlMeasurementValueMismatchSha1(@JsonProperty("expected_value") String expectedValue, @JsonProperty("actual_value") String actualValue) {
        super(new Sha1Digest(expectedValue), new Sha1Digest(actualValue));
    }
}
