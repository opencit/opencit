/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.jersey;

import com.intel.mtwilson.datatypes.Role;
import java.security.Principal;
        
/**
 * In 0.5.2, expanded to include roles for the user.
 * 
 * @since 0.5.1
 * @author jbuhacoff
 */
public class User implements Principal {
    
    private String name;
    private Role[] roles;

    /**
     * Initializes a User object with the given name.
     * 
     * Retained for backward-compatibility with 0.5.1 code that does not
     * include role-based access control.
     * 
     * @since 0.5.1
     * @param name 
     */
    protected User(String name) {
        this.name = name;
        this.roles = new Role[] { };
    }
    
    /**
     * Initializes a User object with the given name and authorized roles.
     * @since 0.5.2
     * @param name
     * @param roles 
     */
    protected User(String name, Role[] roles) {
        this.name = name;
        this.roles = roles;
    }
    
    @Override
    public String getName() {
        return name;
    }

    public Role[] getRoles() {
        return roles;
    }
        
    @Override
    public String toString() { return name; }
    
}
