/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.WebApplicationException;
import org.apache.shiro.authz.AuthorizationException;
import javax.ws.rs.core.Response.Status;

/**
 * Catches org.apache.shiro.authz.AuthorizationException and rethrows as
 * WebApplicationException with a not-authorized status code.
 * Without this filter, clients would get 500 internal server error when
 * the org.apache.shiro.authz.AuthorizationException are thrown.
 * 
 * @author jbuhacoff
 */
public class AuthorizationExceptionFilter implements Filter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthorizationExceptionFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.debug("initializing AuthorizationExceptionFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            log.debug("filtering with AuthorizationExceptionFilter");
            chain.doFilter(request, response);
        }
        catch(RuntimeException e) {
            log.debug("got exception class {}", e.getClass().getName());
            if( e instanceof AuthorizationException ) {
//        catch(AuthorizationException e) { //parent of UnauthenticatedException, UnauthorizedException
            log.info("Denied access to resource: ", e.getMessage());
            Throwable cause = e.getCause();
            while(cause != null ) {
                log.info("Caused by: {}", cause.getMessage());
                cause = cause.getCause();
            }
            throw new WebApplicationException(Status.UNAUTHORIZED);
            }
            else {
                throw e; // rethrow because it's not what we were looking for
            }
        }
    }

    @Override
    public void destroy() {
    }
    
}
