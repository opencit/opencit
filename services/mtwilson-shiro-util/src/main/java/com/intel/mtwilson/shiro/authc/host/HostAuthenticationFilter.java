/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.host;

import com.intel.mtwilson.shiro.AuthenticationFilter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author jbuhacoff
 */
public class HostAuthenticationFilter extends AuthenticationFilter {

    @Override
    protected boolean isAuthenticationRequest(ServletRequest request) {
        if( request instanceof HttpServletRequest ) {
            return true;
        }
        return false;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        HostToken hostToken = new HostToken(httpRequest.getRemoteAddr());
        return hostToken;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        return true;  // override to allow request processing to continue even if user is not from a trusted host (they just won't have those authenticated permissions)
    }
    
    
}
