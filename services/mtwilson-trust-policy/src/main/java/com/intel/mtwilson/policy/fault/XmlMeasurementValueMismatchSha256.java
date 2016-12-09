/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.dcsg.cpg.crypto.Sha256Digest;

/**
 *
 * @author dczech
 */
public class XmlMeasurementValueMismatchSha256 extends XmlMeasurementValueMismatch<Sha256Digest> {
    public XmlMeasurementValueMismatchSha256(Sha256Digest expectedValue, Sha256Digest actualValue) {
        super(expectedValue, actualValue);
    }
    
    @JsonCreator
    public XmlMeasurementValueMismatchSha256(@JsonProperty("expected_value") String expectedValue, @JsonProperty("actual_value") String actualValue) {
        super(new Sha256Digest(expectedValue), new Sha256Digest(actualValue));
    }
}
