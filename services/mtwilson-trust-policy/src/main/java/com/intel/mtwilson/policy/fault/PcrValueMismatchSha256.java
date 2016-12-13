/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mtwilson.model.PcrIndex;

/**
 *
 * @author dczech
 */
public class PcrValueMismatchSha256 extends PcrValueMismatch<Sha256Digest> {
    public PcrValueMismatchSha256(PcrIndex pcrIndex, Sha256Digest expectedValue, Sha256Digest actualValue) {
        super(pcrIndex, expectedValue, actualValue);
    }
    
    @JsonCreator
    public PcrValueMismatchSha256(@JsonProperty("pcr_index") PcrIndex pcrIndex, @JsonProperty("expected_value") String expectedValue, @JsonProperty("actual_value") String actualValue) {
        super(pcrIndex, new Sha256Digest(expectedValue), new Sha256Digest(actualValue));
    }    
}
