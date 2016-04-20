/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.host;

import java.util.Objects;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;

/**
 *
 * @author jbuhacoff
 */
public class HostAuthenticationInfo implements AuthenticationInfo {
    private PrincipalCollection principals;
    private HostFilter credentials;

    @Override
    public PrincipalCollection getPrincipals() {
        return principals;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }
    
            
    public void setPrincipals(PrincipalCollection principals) {
        this.principals = principals;
    }
    
    public void setCredentials(HostFilter credentials) {
        this.credentials = credentials;
    }
    
    @Override
    public int hashCode() {
        return principals == null ? 0 : principals.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HostAuthenticationInfo other = (HostAuthenticationInfo) obj;
        if (!Objects.equals(this.principals, other.principals)) {
            return false;
        }
        if (!Objects.equals(this.credentials, other.credentials)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return principals.toString();
    }
    
}
