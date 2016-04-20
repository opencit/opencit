/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

/**
 * Container for (domain,action,selection) tuples. Shiro's DomainPermission
 * class has many more features and is part of the framework - this 
 * container class is only to hold the permission information for 
 * serializing/deserializing from storage.
 * 
 * Named PermissionInfo to avoid confusion with Shiro's Permission interface.
 * 
 * @author jbuhacoff
 */
public class PermissionInfo {
    private final String domain;
    private final String action;
    private final String selection;

    public PermissionInfo(String domain, String action, String selection) {
        this.domain = domain;
        this.action = action;
        this.selection = selection;
    }

    public String getDomain() {
        return domain;
    }

    public String getAction() {
        return action;
    }

    public String getSelection() {
        return selection;
    }
    
    

    @Override
    public String toString() {
        if( action == null && selection == null ) {
            return domain;
        }
        if( selection == null ) {
            return String.format("%s:%s", domain, action);
        }
        return String.format("%s:%s:%s", domain, action, selection);
    }
    

    public static PermissionInfo parse(String text) {
        String[] parts = text.split(":");
        if( parts.length == 3 ) {
            return new PermissionInfo(parts[0], parts[1], parts[2]);
        }
        if( parts.length == 2 ) {
            return new PermissionInfo(parts[0], parts[1], null);
        }
        if( parts.length == 1 ) {
            return new PermissionInfo(parts[0], null, null);
        }
        throw new IllegalArgumentException("Invalid permission format"); // must be in the form  domain:action:instance or domain:action or domain
    }
}
