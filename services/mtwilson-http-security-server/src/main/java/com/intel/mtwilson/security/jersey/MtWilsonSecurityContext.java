/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.jersey;

import com.intel.mtwilson.datatypes.Role;
import java.security.Principal;
import javax.ws.rs.core.SecurityContext;

/**
 * This class requires the following libraries:
 * javax.ws.rs.core.SecurityContext from a jsr-api.jar
 * @author jbuhacoff
 */
    public class MtWilsonSecurityContext implements SecurityContext {
    private User user;
    private boolean secure = false;
    
    protected MtWilsonSecurityContext(User user, boolean https) {
        this.user = user;
        this.secure = https;
        
    }

    @Override
    public Principal getUserPrincipal() {
        return user;
    }

    @Override
    public boolean isUserInRole(String role) {
        for(Role r : user.getRoles()) {
            if( r.getName().equals(role) ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    /**
     * @return 
     */
    @Override
    public String getAuthenticationScheme() {
        return "X509"; // SecurityContext.DIGEST_AUTH;
    }
    
}
