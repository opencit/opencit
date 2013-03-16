/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.model;

import com.intel.mtwilson.validation.ObjectModel;
import org.codehaus.jackson.annotate.JsonValue;

/**
 *
 * @author jbuhacoff
 * @since 1.2
 */
public class Measurement extends ObjectModel {

    private final Sha1Digest digest;
    private final String label;
    
    public Measurement(Sha1Digest digest, String label) {
        this.digest = digest;
        this.label = label;
    }
    
    public Sha1Digest getValue() { return digest; }
    public String getLabel() { return label; }
    
    @JsonValue
    @Override
    public String toString() {
        return String.format("%s %s", digest.toString(), label);
    }
    

    @Override
    public int hashCode() {
        return digest.hashCode() + label.hashCode();
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
