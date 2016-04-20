/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.password;

import java.util.Objects;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * The SaltedAuthenticationInfo is not implemented because it isn't
 * sufficient to do the comparison.  Instead, this class is intended
 * to work directly with PasswordCredentialsMatcher which expects the
 * credentials to be UserLoginPassword, which specifies the salt, 
 * hash algorithm, and number of iterations.
 * 
 * @author jbuhacoff
 */
public class PasswordAuthenticationInfo implements AuthenticationInfo /*, SaltedAuthenticationInfo*/ {
    private PrincipalCollection principals;
    private HashedPassword credentials;
            
    public void setPrincipals(PrincipalCollection principals) {
        this.principals = principals;
    }
    
    @Override
    public PrincipalCollection getPrincipals() {
        return principals;
    }

    @Override
    public HashedPassword getCredentials() {
        return credentials;
    }
    
    public void setCredentials(HashedPassword credentials) {
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
        final PasswordAuthenticationInfo other = (PasswordAuthenticationInfo) obj;
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

    /*
    @Override
    public ByteSource getCredentialsSalt() {
        if( credentials == null ) {
            return null;
        }
        return ByteSource.Util.bytes(credentials.getSalt());
    }
    */
}
