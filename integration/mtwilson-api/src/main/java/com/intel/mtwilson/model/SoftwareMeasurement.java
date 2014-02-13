/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.fasterxml.jackson.annotation.JsonValue;
//import org.codehaus.jackson.annotate.JsonValue;
import com.intel.dcsg.cpg.crypto.Sha1Digest;

/**
 *
 * @author jbuhacoff
 * @since 1.2
 */
public class SoftwareMeasurement extends Measurement {

    private final String vendor;
    private final String name;
    private final String version;
    
    public SoftwareMeasurement(Sha1Digest digest, String vendor, String name, String version) {
        super(digest, String.format("%s %s %s", vendor, name, version));
        this.vendor = vendor;
        this.name = name;
        this.version = version;
    }
    
    public String getVendor() { return vendor; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    
//    @JsonValue
    @Override
    public String toString() {
        return String.format("%s %s %s", vendor, name, version);
    }
    

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(17,53);
        return builder.append(vendor).append(name).append(version).toHashCode();
    }

    /**
     * Returns true only if the PcrIndex and PcrValue of this object and the other
     * object are identical.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object other) {
        if( other == null ) { return false; }
        if( other == this ) { return true; }
        if( other.getClass() != this.getClass() ) { return false; }
        SoftwareMeasurement rhs = (SoftwareMeasurement)other;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(getValue(), rhs.getValue());
        builder.append(vendor, rhs.vendor);
        builder.append(name, rhs.name);
        builder.append(version, rhs.version);
        return builder.isEquals();
    }
    
    @Override
    protected void validate() {
        super.validate();
        if( vendor == null ) { fault("Software vendor is null"); }
        if( name == null ) { fault("Software name is null"); }
        if( version == null ) { fault("Software version is null"); }
    }
    
}
