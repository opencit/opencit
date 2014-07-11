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
    
    private boolean skipAuthenticated = true;
    private boolean permissive = true;
    
    /**
     * Default value is true;  set  .skipAuthenticated=false in shiro.ini to
     * change it.
     * 
     * @return true if this filter will skip processing for subjects that are already authenticated
     */
    public boolean isSkipAuthenticated() {
        return skipAuthenticated;
    }

    public void setSkipAuthenticated(boolean skipAuthenticated) {
        this.skipAuthenticated = skipAuthenticated;
    }

    /**
     * Permissive mode allows chaining of authentication filters so that
     * clients can choose one of a number of ways to authenticate; each
     * filter in permissive mode allows processing to continue to other filters.
     * 
     * @return true if processing should continue when the subject is not attempting to login in a way that this filter understands
     */
    public boolean isPermissive() {
        return permissive;
    }

    public void setPermissive(boolean permissive) {
        this.permissive = permissive;
    }
    
    
    
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
        if( SecurityUtils.getSubject().isAuthenticated() && isSkipAuthenticated()  /*&& !isMultifactorAuthentication()*/ ) {
            log.debug("Subject is authenticated; skipping {}", getClass().getName()); // and multifactor authentication is not enabled");
            return true;
        }
        try {
            if( isAuthenticationRequest(request) ) {
                log.debug("Detected authentication request for {}", getClass().getName());
                if( authenticate(request, response, mappedValue) ) {
                    log.debug("Authentication ok");
                    return true;
                }
            }
            if( isPermissive() ) {
                // in permissive mode we let the request continue (default true) - a "user" filter later on or an authorization check will stop the request if it cannot continue without being authenticated
                return true;
            }
            return false;
        }
        catch(Exception e) {
            log.debug("Unexpected error while authenticating request", e);
            return false; // an internal error in the authentication system should cause the request to be rejected
        }
    }
    
    protected boolean authenticate(ServletRequest request, ServletResponse response, Object mappedValue) {
        try {
            AuthenticationToken token = createToken(request);
            if( token != null ) {
                try {
                    Subject subject = SecurityUtils.getSubject();
                    subject.login(token);
                    log.debug("login success for filter {}", getClass().getName());
                    onLoginSuccess(token, subject, request, response);
                    return true;
                }
                catch(AuthenticationException e) {
                    log.debug("login failure for filter {}", getClass().getName());
                    onLoginFailure(token, e, request, response);
                    return false;
                }
            }
            throw new IllegalStateException("A valid non-null "+
                    "AuthenticationToken must be returned from createToken "+
                    "in order to execute a login attempt.");
        }
        catch(Exception e) {
            log.error("Authentication error", e);
            return false;
        }
    }
    
    /**
     * 
     * @return true if the request contains elements that are recognized as an authentication attempt for this filter, such as an Authorization header with a specific protocol, or a cookie, etc. implying that an authentication token can be created from the request;  false if the request does not contain an authentication attempt recognized by this filter
     */
    abstract protected boolean isAuthenticationRequest(ServletRequest request);
    
    /**
     * 
     * @param request
     * @return a valid authentication token or null if one cannot be created
     */
    abstract protected AuthenticationToken createToken(ServletRequest request);
    
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
