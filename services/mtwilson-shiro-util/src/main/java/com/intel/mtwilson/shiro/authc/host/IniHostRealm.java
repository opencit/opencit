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
 * TODO: create database table for trusted hosts and create JdbcHostRealm to
 * look them up in the database as an alternative to this class which reads them
 * from the ini file; in the database the administrator could assign different
 * ip addresses to different users or just a list of ip address ranges that can
 * each have different permissions (and give each range a name which would be
 * the username) - which is better than the hardcoded superuser permission here
 * (but not more secure, since we cannot verify the remote address in the tcp
 * packet is correct - this feature relies on network security features outside
 * the application) Table might be like this: mw_user_login_host (id, user_id,
 * host, role[])
 *
 *
 *
 * @author jbuhacoff
 */
public class IniHostRealm extends AuthorizingRealm {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IniHostRealm.class);
    private String allow;
    private HostAllowCsvFilter hostAllowCsvFilter;

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
        // for informational purposes only:
        Collection<LoginHostPrincipal> principals = pc.byType(LoginHostPrincipal.class);
        for (LoginHostPrincipal principal : principals) {
            log.debug("doGetAuthorizationInfo for host: {}", principal.getHost());
            String address = principal.getHost(); // NOTE: only for the shiro.ini configuration, we use the hostname as the username;  if this moves to the database a trusted remote host might be associated with an existing user record 
            // TODO: should only return *:* credentials if the principal is a host in the "allow" list ...
            //       so we could do that check HERE , even though it should go in the credentials matcher...
            //       need a real principal for the host thing... shouldn't just rely on username...
            log.debug("checking remote address {} is in allowed list again", address);
            if (hostAllowCsvFilter.accept(address)) { // NOTE: when trusted host addresses are stored in the database, instead of using this filter here just checking the db to see if the host address is there accomplishes the same thing
                // TODO:  when moving this to mtwilson-shiro-jdbi, each user might have
                //        one or more trust host logins assigned with different permissions,
                //        or the administrator might create a user with a name like
                //        "localhost" and assign it host names 127.0.0.1,localhost and 
                //        give it one set of permissions, and then create another user 
                //        with a name like "noc" and assign it an ip address range,
                //        and give it a second set of permissions.
                authzInfo.addStringPermission("*:*");
            }
        }
        return authzInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
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
