/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.model;

import com.intel.mtwilson.validation.ObjectModel;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonValue;

/**
 *
 * @author jbuhacoff
 * @since 1.2
 */
public class Measurement extends ObjectModel {

    private final Sha1Digest digest;
    private final String label;
    private final HashMap<String,String> info = new HashMap<String,String>();
    
    public Measurement(Sha1Digest digest, String label) {
        this.digest = digest;
        this.label = label;
    }

    public Measurement(Sha1Digest digest, String label, Map<String,String> info) {
        this.digest = digest;
        this.label = label;
        this.info.putAll(info);
    }
    
    public Sha1Digest getValue() { return digest; }
    public String getLabel() { return label; } // intended to summarize the measurement's origin or purpose in one line... you can put additional information in "info"
    public Map<String,String> getInfo() { return info; } // other information, such as what vmware provides with each measurement
    
//    @JsonValue
    @Override
    public String toString() {
        return String.format("%s %s", digest.toString(), label);
    }
    

    @Override
    public int hashCode() {
        return digest.hashCode() + label.hashCode(); // two measurements are equal if their digests and labels are equal...  this property facilitates very convenient management of measurement using java's collections, such as contains(measurement) and removeAll(list of measurements) where one side comes from the host and may have a different label than what got saved in the database 
    }

    /**
     * Returns true only if the PcrIndex and PcrValue of this object and the other
     * object are identical.
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
        if ((this.label == null) ? (other.label != null) : !this.label.equals(other.label)) {
            return false;
        }
        return true;
    }
    
    @Override
    protected void validate() {
        if( digest == null ) { fault("SHA1 Digest is null"); }
        else if (!digest.isValid()) { fault(digest, "Invalid measurement value"); }
        if( label == null ) { fault("Measurement label is null"); }
    }
    
}
