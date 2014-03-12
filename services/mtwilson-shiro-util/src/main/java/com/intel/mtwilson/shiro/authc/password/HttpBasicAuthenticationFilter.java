/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.password;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;

/**
 *
 * @author jbuhacoff
 */
public class HttpBasicAuthenticationFilter extends BasicHttpAuthenticationFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HttpBasicAuthenticationFilter.class);
    
    public HttpBasicAuthenticationFilter() {
        super();
    }
    
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        log.debug("executeLogin");
        AuthenticationToken token = createToken(request, response);
        if (token == null) {
            log.debug("executeLogin token is null");
            String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken "
                    + "must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        }
        log.debug("executeLogin authentication token {}", token.getClass().getName());
        try {
            Subject subject = getSubject(request, response);
            log.debug("executeLogin subject {}", subject.getClass().getName());
            subject.login(token);
            log.debug("executeLogin subject login ok");
            return onLoginSuccess(token, subject, request, response);
        } catch (AuthenticationException e) {
            log.debug("executeLogin subject login failed {}", e.getMessage());
            return onLoginFailure(token, e, request, response);
        }
    }
}
