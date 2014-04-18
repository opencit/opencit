/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.x509;

import java.security.cert.X509Certificate;
import java.util.Objects;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * Encapsulates the X509 certificate login information from the database
 * for a given user. This is used to verify the signature on an incoming
 * request.
 * 
 * @author jbuhacoff
 */
public class X509AuthenticationInfo implements AuthenticationInfo {
    private PrincipalCollection principals;
    private X509Certificate credentials;
            
    public void setPrincipals(PrincipalCollection principals) {
        this.principals = principals;
    }
    
    @Override
    public PrincipalCollection getPrincipals() {
        return principals;
    }

    @Override
    public X509Certificate getCredentials() {
        return credentials;
    }
    
    public void setCredentials(X509Certificate credentials) {
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
        final X509AuthenticationInfo other = (X509AuthenticationInfo) obj;
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
