/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.host;

import com.intel.mtwilson.shiro.*;
import java.util.Collection;
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
 * @author jbuhacoff
 */
public class IniHostRealm extends AuthorizingRealm {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IniHostRealm.class);
    private String allow = null;
    private HostAllowCsvFilter hostAllowCsvFilter = null;

    public void setAllow(String allow) {
        this.allow = allow;
        this.hostAllowCsvFilter = new HostAllowCsvFilter(allow);
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
        if( hostAllowCsvFilter == null ) {
            log.warn("IniHostRealm.allow is not set; host-based authorization disabled");
            return authzInfo;
        }
        Collection<LoginHostPrincipal> principals = pc.byType(LoginHostPrincipal.class);
        for (LoginHostPrincipal principal : principals) {
            log.debug("doGetAuthorizationInfo for host: {}", principal.getHost());
            String address = principal.getHost(); // NOTE: only for the shiro.ini configuration, we use the hostname as the username;  if this moves to the database a trusted remote host might be associated with an existing user record 
            log.debug("checking remote address {} is in allowed list again", address);
            if (hostAllowCsvFilter.accept(address)) { // NOTE: when trusted host addresses are stored in the database, instead of using this filter here just checking the db to see if the host address is there accomplishes the same thing
                authzInfo.addStringPermission("*:*");
            }
        }
        return authzInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if( hostAllowCsvFilter == null ) {
            log.warn("IniHostRealm.allow is not set; host-based authentication disabled");
            return null;
        }
        if (token instanceof HostToken) {
            HostToken xToken = (HostToken) token;
            String address = xToken.getHost();
            log.debug("doGetAuthenticationInfo for address {}", address);
            // in a jdbc version we might include information in the token like 
            // whether it's a ipv4 or ipv6 address and use that here to look up a 
            // different table in the database; but this realm is configured from
            // the shiro.ini file 

            // if the host mentioned in the token is on the allow list, we return
            // authentication info;  otherwise we return null as if that user was not
            // found in the repository
            if (hostAllowCsvFilter.accept(address)) {
                log.debug("remote address {} is in allow list {}", address, allow);
                SimplePrincipalCollection principals = new SimplePrincipalCollection();
//                principals.add(new Username((String) xToken.getPrincipal()), getName());
                principals.add(new LoginHostPrincipal(xToken.getHost()), getName());
                HostAuthenticationInfo info = new HostAuthenticationInfo();
                info.setPrincipals(principals);
                info.setCredentials(hostAllowCsvFilter); // new HostAllowCsvFilter(allow));
                return info;
            }
            log.debug("remote address {} is not in allow list", address);
            return null;
        } else {
            log.debug("token is not a host token");
            return null;
        }
    }
}
