/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.token;

import com.intel.mtwilson.shiro.authc.x509.*;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.util.WebUtils;
import com.intel.mtwilson.shiro.HttpAuthenticationFilter;

/**
 * Handles authentication via an HTTP Authorization header with the "Token" 
 * keyword by looking up the user and permissions associated with the token.
 * Using tokens, it is possible to grant temporary permissions to
 * existing users without affecting their permanent permission settings,
 * or to grant temporary permissions to non-users without requiring them
 * to register as users.
 * 
 * The token specified in the authorization header is a literal - it is not
 * processed in any way before passing to the lookup function.
 * 
 * For example:
 * <pre>
 * Authorization: Token {token}
 * </pre>
 * 
 * Known issues: conformance with rfc7235
 * 
 * @author jbuhacoff
 */
public class TokenAuthenticationFilter extends HttpAuthenticationFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509AuthenticationFilter.class);

    public TokenAuthenticationFilter() {
        super();
        setAuthenticationScheme("Token");
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request) {
        log.debug("createToken");
        try {
            HttpServletRequest httpRequest = WebUtils.toHttp(request);
            Token token = getToken(httpRequest);
            
            TokenAuthenticationToken authenticationToken = new TokenAuthenticationToken(token, request.getRemoteAddr());
            log.debug("createToken: returning TokenAuthenticationToken");
            return authenticationToken;
        } catch (Exception e) {
            throw new AuthenticationException("Cannot authenticate request: " + e.getMessage(), e);
        }
    }

    private Token getToken(HttpServletRequest httpRequest) {
        String authorizationText = httpRequest.getHeader(getAuthorizationHeaderName());
        log.debug("Parsing authorization header: {}", authorizationText);
        
        // splitting on spaces should yield "Token" followed by a literal
        String[] terms = authorizationText.split(" ");
        if( terms.length == 0 ) {
            throw new IllegalArgumentException("Authorization header is empty");
        }
        if (!"Token".equals(terms[0])) {
            throw new IllegalArgumentException("Authorization type is not Token");
        }
        if( terms.length != 2 ) {
            throw new IllegalArgumentException("Authorization header format invalid for Token");
        }
        if( terms[1].isEmpty() ) {
            throw new IllegalArgumentException("Authorization token is missing");
        }
        log.debug("Got token {}", terms[1]);
        return new Token(terms[1]);
    }

}
