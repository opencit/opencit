/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.mtwilson.model.PcrIndex;

/**
 *
 * @author dczech
 */
public class PcrValueMismatchSha1 extends PcrValueMismatch<Sha1Digest> {
    public PcrValueMismatchSha1(PcrIndex pcrIndex, Sha1Digest expectedValue, Sha1Digest actualValue) {
        super(pcrIndex, expectedValue, actualValue);
    }
    
    @JsonCreator
    public PcrValueMismatchSha1(@JsonProperty("pcr_index") PcrIndex pcrIndex, @JsonProperty("expected_value") String expectedValue, @JsonProperty("actual_value") String actualValue) {
        super(pcrIndex, new Sha1Digest(expectedValue), new Sha1Digest(actualValue));
    }
}
