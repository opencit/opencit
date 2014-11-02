/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x500;

import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jbuhacoff
 */
public class DNBuilder {
    public static DNBuilder factory() { return new DNBuilder(); }
    
    private String commonName = null;
    private String organizationUnit = null;
    private String organizationName = null;
    private String country = null;
    
    public DNBuilder commonName(String commonName) {
        this.commonName = commonName;
        return this;
    }
    public DNBuilder organizationUnit(String organizationUnit) {
        this.organizationUnit = organizationUnit;
        return this;
    }
    public DNBuilder organizationName(String organizationName) {
        this.organizationName = organizationName;
        return this;
    }
    public DNBuilder country(String country) {
        this.country = country;
        return this;
    }
    
    @Override
    public String toString() {
        ArrayList<String> parts = new ArrayList<String>();
        if(commonName != null) { parts.add(String.format("CN=%s",commonName)); }
        if(organizationUnit != null) { parts.add(String.format("OU=%s",organizationUnit)); }
        if(organizationName != null) { parts.add(String.format("O=%s",organizationName)); }
        if(country != null) { parts.add(String.format("C=%s",country)); }
        return StringUtils.join(parts, ",");
    }

}
