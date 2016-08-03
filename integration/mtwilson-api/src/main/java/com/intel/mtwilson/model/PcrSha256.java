/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
/**
 *
 * @author dczech
 */
public class PcrSha256 extends Pcr<Sha256Digest> {
    private final Sha256Digest pcrValue;    
    
    public PcrSha256(PcrIndex pcrNumber, byte[] value) {
        super(pcrNumber);
        pcrValue = new Sha256Digest(value);     
    }
    
    @JsonCreator
    public PcrSha256(@JsonProperty("index") int pcrNumber, @JsonProperty("value") String value) {
        super(PcrIndex.valueOf(pcrNumber));
        pcrValue = new Sha256Digest(value);   
    }
    
    public PcrSha256(PcrIndex pcrNumber, String value) {
        super(pcrNumber);
        pcrValue = new Sha256Digest(value);   
    }
    
    @Override
    public Sha256Digest getValue() {
        return pcrValue;
    }

    @Override
    public DigestAlgorithm getPcrBank() {
        return DigestAlgorithm.SHA256;
    }

    @Override
    protected void validateOverride() {
        if(!Sha256Digest.isValid(pcrValue.toByteArray())) {
            fault("Invalid SHA256 PCR Value");
        }
    }    
}
