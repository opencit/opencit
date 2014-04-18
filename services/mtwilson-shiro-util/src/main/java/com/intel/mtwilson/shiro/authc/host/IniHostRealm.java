/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.host;

import com.intel.mtwilson.shiro.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * TODO: create database table for trusted hosts and create JdbcHostRealm to 
 * look them up in the database as an alternative to this class which reads
 * them from the ini file;  in the database the administrator could assign
 * different ip addresses to different users or just a list of ip address ranges
 * that can each have different permissions (and give each range a name which
 * would be the username) - which is better than the hardcoded superuser 
 * permission here
 * 
 * @author jbuhacoff
 */
public class IniHostRealm extends AuthorizingRealm {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IniHostRealm.class);
    private String allow;

    public void setAllow(String allow) {
        this.allow = allow;
    }
    
    
    
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof HostToken;
    }
    
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {
        if (pc == null) {
            throw new AuthorizationException("Principal must be provided");
        }
        SimpleAuthorizationInfo authzInfo = new SimpleAuthorizationInfo();
        for (String realmName : pc.getRealmNames()) {
            log.debug("doGetAuthorizationInfo for realm: {}", realmName);
        }
        Collection<Username> usernames = pc.byType(Username.class);
        for (Username username : usernames) {
            log.debug("doGetAuthorizationInfo for username: {}", username.getUsername());
        }
        // TODO:  when moving this to mtwilson-shiro-jdbi, each user might have
        //        one or more trust host logins assigned with different permissions,
        //        or the administrator might create a user with a name like
        //        "localhost" and assign it host names 127.0.0.1,localhost and 
        //        give it one set of permissions, and then create another user 
        //        with a name like "noc" and assign it an ip address range,
        //        and give it a second set of permissions.
        authzInfo.addStringPermission("*:*");
        return authzInfo;
    }
    
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        HostToken xToken = (HostToken) token;
        if( xToken.getPrincipal() instanceof String) {
            String address = (String)xToken.getPrincipal();
            log.debug("doGetAuthenticationInfo for address {}", address);
        }
        // we don't really care about the token here since in this implementation
        // there is only one filter (HostAllowCsvFilter) which has the list of
        // allowed addresses configured in shiro.ini - so we always use that list
        // in a jdbc version we might include information in the token like 
        // whether it's a ipv4 or ipv6 address and use that here to look up a 
        // different table in the database
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add(new Username((String)xToken.getPrincipal()), getName());
        HostAuthenticationInfo info = new HostAuthenticationInfo();
        info.setPrincipals(principals);
        info.setCredentials(new HostAllowCsvFilter(allow));
        return info;
    }
}
