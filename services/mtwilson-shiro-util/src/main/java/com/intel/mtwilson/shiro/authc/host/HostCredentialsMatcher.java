/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.host;

import com.intel.dcsg.cpg.net.InternetAddress;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

/**
 *
 * @author jbuhacoff
 */
public class HostCredentialsMatcher implements CredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        if( token == null || token.getCredentials() == null ) { return false; }
        if( !(token.getCredentials() instanceof String) ) { return false; }
        if( info == null || info.getCredentials() == null ) { return false; }
        if( !(info.getCredentials() instanceof HostFilter) ) { return false; }        
        String address = (String)token.getCredentials();
        HostFilter filter = (HostFilter)info.getCredentials();
        return filter.accept(address);
    }
    
}
