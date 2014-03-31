/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;

/**
 * This AuthenticationFilter class is a replacement for Apache Shiro's
 * AccessControlFilter, AuthenticationFilter, and AuthenticatingFilter 
 * class hierarchy which also extends PathMatchingFilter.
 * 
 * The problem being solved here is the unclear programming interface for
 * authentication filters. The AccessControlFilter's onPreHandle method
 * returns isAccessAllowed || onAccessDenied, where the isAccessAllowed
 * method is defined by AuthenticationFilter to be subject.isAuthenticated
 * and the onAccessDenied method is defined by BasicHttpAuthenticationFilter
 * as loggedIn = false; if(isLoginAttempt) { loggedIn = executeLogin; } 
 * if(!loggedIn) { sendChallenge; } 
 * return loggedIn;   and that seems like logic that should be in a
 * superclass not in a concrete authentication method because every 
 * authentication method will need to evaluate whether the request is
 * a login attempt, whether it's successful, and then to send the
 * challenge if it's not successful.  In addition, the AccessControlFilter's
 * logic of allowing the request to pass through if the subject has already
 * been authenticated precludes the use of multi-factor authentication because
 * only one authentication filter would have a chance to handle the
 * request. 
 * 
 * Instead, this AuthenticationFilter class encapsulates the following logic:
 * If the subject is already authenticated, and if multifactor authentication
 * if not enabled, then continue the request (and skip this filter)
 * If the subject is already authenticated, and if multifactor authentication
 * is enabled, then attempt to authenticate the user
 * If the subject is not already authenticated, then attempt to authenticate
 * the user
 * When attempting to authenticate the user, check if the request is one that
 * we know how to authenticate and only proceed if it is known
 * 
 * 
 * @author jbuhacoff
 */
public abstract class AuthenticationFilter extends PathMatchingFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthenticationFilter.class);
/*
    private boolean multifactorAuthentication = false;

    public void setMultifactorAuthentication(boolean multifactorAuthentication) {
        this.multifactorAuthentication = multifactorAuthentication;
    }

    public boolean isMultifactorAuthentication() {
        return multifactorAuthentication;
    }*/
    
    /**
             in multifactor authentication mode we always look for an authentication
             token in the request and if we find one we attempt to authenticate with
             it and add the credentials to the subject if successful
     * 
     * @param request
     * @param response
     * @param mappedValue
     * @return true if isMultifactorAuthentication and not isAuthenticationRequest or if subject.isAuthenticated
     * @throws Exception 
     */
    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        log.debug("onPreHandle");
        if( SecurityUtils.getSubject().isAuthenticated() /*&& !isMultifactorAuthentication()*/ ) {
            log.debug("Subject is authenticated"); // and multifactor authentication is not enabled");
            return true;
        }
        if( isAuthenticationRequest(request) ) {
            return authenticate(request, response, mappedValue);
        }
        return true; // it's not a request we know how to authenticate so let it continue - a "user" filter later on or an authorization check will stop the request if it cannot continue without being authenticated
    }
    
    protected boolean authenticate(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        AuthenticationToken token = createToken(request);
        if( token == null ) {
            throw new IllegalStateException("A valid non-null "+
                    "AuthenticationToken must be returned from createToken "+
                    "in order to execute a login attempt.");
        }
        try {
            Subject subject = SecurityUtils.getSubject();
            subject.login(token);
            return onLoginSuccess(token, subject, request, response);
        }
        catch(AuthenticationException e) {
            return onLoginFailure(token, e, request, response);
        }
    }
    
    /**
     * 
     * @return true if the request contains elements that are recognized as an authentication attempt for this filter, such as an Authorization header with a specific protocol, or a cookie, etc.;  false if the request does not contain an authentication attempt recognized by this filter
     */
    abstract protected boolean isAuthenticationRequest(ServletRequest request);
    
    abstract protected AuthenticationToken createToken(ServletRequest request) throws Exception;
    
    /**
     * Always returns true; subclasses can override this behavior.
     * 
     * @param token
     * @param subject
     * @param request
     * @param response
     * @return true if the request should continue processing, or false if processing should stop
     */
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) {
        return true;
    }
    
    /**
     * Always returns false; subclasses can override this behavior.
     * 
     * @param token
     * @param e
     * @param request
     * @param response
     * @return true if the request should continue processing, or false if processing should stop
     */
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        return false;
    }
}
