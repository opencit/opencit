/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.jersey;

import com.intel.mtwilson.model.Md5Digest;
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
    private String loginName = "";
    private Md5Digest md5Hash;

    /**
     * Initializes a User object with the given name, authorized roles and login Name. The name would be the fingerprint. Since
     * we need to store the log in name of the user also, we are storing the same.
     * @since MW 1.1 Release
     * @param name
     * @param roles 
     * @param loginName
     */
    protected User(String name, Role[] roles, String loginName, Md5Digest md5Hash) {
        this.name = name;
        this.roles = roles;
        if( loginName != null ) { this.loginName = loginName; }
        this.md5Hash = md5Hash;
    }
    
    @Override
    public String getName() {
        return name;
    }

    public Role[] getRoles() {
        return roles;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
    
    public Md5Digest getMd5Hash() { return md5Hash; }
    
    @Override
    public String toString() { return name; }
    
}
