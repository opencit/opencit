/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.model;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import java.util.Map;

/**
 *
 * @author dczech
 */
public class MeasurementSha256 extends Measurement<Sha256Digest> {
    
    public MeasurementSha256(Sha256Digest digest, String label) {
        super(digest, label);
    }
    
    @JsonCreator
    public MeasurementSha256(@JsonProperty("value") Sha256Digest digest, @JsonProperty("label") String label, @JsonProperty("info") Map<String, String> info) {
        super(digest, label, info);
    }

    @Override
    protected void validateOverride() {
        if(!Sha256Digest.isValid(this.digest.toByteArray())) {
            fault("SHA256 Digest is invalid");
        }
    }
    
}
