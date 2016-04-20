/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.model;

/**
 *
 * @author jbuhacoff
 */
public class OID {
    public static final String HOST_UUID = "2.25";
    public static final String NAMEVALUE_UTF8 = "2.5.4.789.1";
    
    private String oid;
    private String name;
    private String description;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
}
