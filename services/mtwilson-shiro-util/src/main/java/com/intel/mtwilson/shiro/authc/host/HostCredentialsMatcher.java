/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.host;

import com.intel.dcsg.cpg.net.InternetAddress;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.HostAuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

/**
 *
 * @author jbuhacoff
 */
public class HostCredentialsMatcher implements CredentialsMatcher {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostCredentialsMatcher.class);
    
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        if( token == null || token.getCredentials() == null ) { return false; }
        if( !(token instanceof HostAuthenticationToken) ) { return false; }
        if( info == null || info.getCredentials() == null ) { return false; }
        if( !(info.getCredentials() instanceof HostFilter) ) { return false; }
        HostAuthenticationToken hostAuthenticationToken = (HostAuthenticationToken)token;
        String address = hostAuthenticationToken.getHost();
        log.debug("doCredentialsMatch checking address {} from token {}", address, token.getClass().getName());
        HostFilter filter = (HostFilter)info.getCredentials();
        boolean accept = filter.accept(address);
        log.debug("hostfilter accept {}: {}", address, accept);
        return accept;
    }
    
}
