/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.file.model;

/**
 *
 * @author jbuhacoff
 */
public class UserPermission {
    private String domain;
    private String action;
    private String instance;

    public UserPermission(String domain, String action, String instance) {
        this.domain = domain;
        this.action = action;
        this.instance = instance;
    }

    @Override
    public String toString() {
        if( action == null && instance == null ) {
            return domain;
        }
        if( instance == null ) {
            return String.format("%s:%s", domain, action);
        }
        return String.format("%s:%s:%s", domain, action, instance);
    }
    

    /**
     * The permission text must be in the form  "domain:action:instance"
     * or "domain:action" or "domain"
     * 
     * @param text
     * @return 
     */
    public static UserPermission parse(String text) {
        String[] parts = text.split(":");
        if( parts.length == 3 ) {
            return new UserPermission(parts[0], parts[1], parts[2]);
        }
        if( parts.length == 2 ) {
            return new UserPermission(parts[0], parts[1], null);
        }
        if( parts.length == 1 ) {
            return new UserPermission(parts[0], null, null);
        }
        throw new PermissionFormatException(text); 
    }
}
