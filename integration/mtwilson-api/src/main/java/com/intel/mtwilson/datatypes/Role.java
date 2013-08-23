/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

/**
 * There are existing implementations of access control lists and role-based
 * access, but until one is selected for implementation this simple role
 * class is used as an API datatype as well. 
 * 
 * @author jbuhacoff
 */
public enum Role {
    Security("Security"),
    Whitelist("Whitelist"),
    Attestation("Attestation"),
    Cache("Cache"),
    Report("Report"),
    Audit("Audit"),
    AssetTagManagement("AssetTagManagement");
    
    private String name;
    
    public String getName() { return name; }
    
    private Role(String name) {
        this.name = name;
    }
}
