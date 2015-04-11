/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import java.util.Collections;
import java.util.Set;

/**
 * A principal (user identity) that includes both the authenticated username
 * and the permissions associated with the user. 
 * 
 * @author jbuhacoff
 */
public class UsernameWithPermissions extends Username {
    private Set<String> permissions;

    public UsernameWithPermissions(String username, Set<String> permissions) {
        super(username);
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    public Set<String> getPermissions() {
        return permissions;
    }
    
}
