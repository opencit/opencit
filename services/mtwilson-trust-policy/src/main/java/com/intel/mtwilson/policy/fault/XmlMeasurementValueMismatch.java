/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.intel.dcsg.cpg.crypto.AbstractDigest;
import com.intel.mtwilson.model.PcrIndex;
//import com.intel.mtwilson.model.Sha1Digest;
import com.intel.mtwilson.policy.Fault;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.crypto.DigestAlgorithm;

/**
 *
 * @author jbuhacoff
 * @param <T>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
              include = JsonTypeInfo.As.PROPERTY,
              property = "digest_type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = XmlMeasurementValueMismatchSha1.class),
    @JsonSubTypes.Type(value = XmlMeasurementValueMismatchSha256.class)
})
public abstract class XmlMeasurementValueMismatch<T extends AbstractDigest> extends Fault {
    private T expectedValue;
    private T actualValue;
    
    public XmlMeasurementValueMismatch() { } // for desearializing jackson
    
    protected XmlMeasurementValueMismatch(T expectedValue, T actualValue) {
        super("Host XML measurement log final hash with value %s does not match expected value %s", actualValue.toString(), expectedValue.toString());
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
    }
    
    public static XmlMeasurementValueMismatch newInstance(DigestAlgorithm bank, AbstractDigest expectedValue, AbstractDigest actualValue) {
        switch(bank) {
            case SHA1:
                return new XmlMeasurementValueMismatchSha1((Sha1Digest)expectedValue, (Sha1Digest)actualValue);
            case SHA256:
                return new XmlMeasurementValueMismatchSha256((Sha256Digest)expectedValue, (Sha256Digest)actualValue);
            default:
                throw new UnsupportedOperationException("Not supported yet");
        }
    }
    
    public T getExpectedValue() { return expectedValue; }
    public T getActualValue() { return actualValue; }
}
