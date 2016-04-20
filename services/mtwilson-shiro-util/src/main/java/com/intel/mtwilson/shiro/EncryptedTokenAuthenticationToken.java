/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 *
 * @author jbuhacoff
 */
public class EncryptedTokenAuthenticationToken implements AuthenticationToken {
    private Username principal;
    private String encryptedToken;
    private String host;
    
    public EncryptedTokenAuthenticationToken(Username principal, String encryptedToken) {
        this.principal = principal;
        this.encryptedToken = encryptedToken;
        this.host = null;
    }
    public EncryptedTokenAuthenticationToken(Username principal, String encryptedToken, String host) {
        this.principal = principal;
        this.encryptedToken = encryptedToken;
        this.host = host;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return encryptedToken;
    }

    public String getHost() {
        return host;
    }
    
    
    
}
