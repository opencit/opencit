/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.token;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * The TokenAuthenticationToken provides the token literal as both the
 * principal and the credentials. The token literal is all non-whitespace
 * text after the the "Token" keyword in the HTTP Authorization header.
 * When the token is verified against the TokenAuthenticationInfo from
 * the database, the literals are compared using a String equals method.
 * 
 * @author jbuhacoff
 */
public class TokenAuthenticationToken implements AuthenticationToken {
    private Token token;
    private String host;
    
    public TokenAuthenticationToken(Token token, String host) {
        this.token = token;
        this.host = host;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    public Token getToken() {
        return token;
    }
    
    public String getHost() {
        return host;
    }
    
}
