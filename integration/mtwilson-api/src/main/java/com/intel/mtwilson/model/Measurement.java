/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.intel.dcsg.cpg.crypto.AbstractDigest;
import com.intel.dcsg.cpg.validation.ObjectModel;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author jbuhacoff
 * @param <T>
 * @since 1.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "digest_type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MeasurementSha1.class),
    @JsonSubTypes.Type(value = MeasurementSha256.class)
})
public abstract class Measurement<T extends AbstractDigest> extends ObjectModel {

    protected final T digest;
    protected final String label;
    protected final HashMap<String, String> info = new HashMap<>();

    protected Measurement(T digest, String label) {
        this.digest = digest;
        this.label = label;
    }

    @JsonCreator
    protected Measurement(@JsonProperty("value") T digest, @JsonProperty("label") String label, @JsonProperty("info") Map<String, String> info) {
        this(digest, label);
        if (info != null) {
            this.info.putAll(info);
        }
    }

    public T getValue() {
        return digest;
    }

    public String getLabel() {
        return label;
    } // intended to summarize the measurement's origin or purpose in one line... you can put additional information in "info"

    public Map<String, String> getInfo() {
        return info;
    } // other information, such as what vmware provides with each measurement

//    @JsonValue
    @Override
    public String toString() {
        return String.format("%s %s", digest.toString(), label);
    }

    @Override
    public int hashCode() {
        return digest.hashCode(); // two measurements are equal if their digests are equal...  the labels are arbitrary; this property facilitates very convenient management of measurement using java's collections, such as contains(measurement) and removeAll(list of measurements) where one side comes from the host and may have a different label than what got saved in the database 
    }

    /**
     * Returns true only if the digest of this object and the other object are equal
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Measurement other = (Measurement) obj;
        if ((this.digest == null) ? (other.digest != null) : !this.digest.equals(other.digest)) {
            return false;
        }
        return true;
    }

    @Override
    protected final void validate() {
        if (digest == null) {
            fault("Digest is null");
        }
        if (label == null) {
            fault("Measurement label is null");
        }
        validateOverride();
    }

    protected abstract void validateOverride();

}
