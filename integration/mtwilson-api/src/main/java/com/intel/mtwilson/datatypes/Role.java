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
    // v2 API roles. Adding here for displaying the same in the portal.
    AssetTagManagement("AssetTagManagement"),
    UserManager("user_manager"),
    HostManager("host_manager"),
    Administrator("administrator"),
    WhitelistManager("whitelist_manager"),
    ServerManager("server_manager"),
    ReportManager("report_manager"),
    Auditor("auditor"),
    AssetTagManager("asset_tag_manager"),
    Challenger("challenger"),
    TlsPolicyManager("tls_policy_manager");
    
    private String name;
    
    public String getName() { return name; }
    
    private Role(String name) {
        this.name = name;
    }
}
