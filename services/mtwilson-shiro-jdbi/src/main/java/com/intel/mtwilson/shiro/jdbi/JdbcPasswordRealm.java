/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.shiro.UserId;
import com.intel.mtwilson.shiro.Username;
import com.intel.mtwilson.shiro.authc.password.LoginPasswordId;
import com.intel.mtwilson.shiro.authc.password.PasswordAuthenticationInfo;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.shiro.jdbi.model.Role;
import com.intel.mtwilson.shiro.jdbi.model.RolePermission;
import com.intel.mtwilson.shiro.jdbi.model.UserLoginPassword;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.JdbcUtils;

/**
 * TODO: move into mtwilson-shiro-jdbi
 * 
 * @author jbuhacoff
 */
public class JdbcPasswordRealm extends AuthorizingRealm {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JdbcPasswordRealm.class);
    
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
        try (LoginDAO dao = MyJdbi.authz()) {
            
            Collection<LoginPasswordId> loginPasswordIds = pc.byType(LoginPasswordId.class);
            for (LoginPasswordId loginPasswordId : loginPasswordIds) {
                log.debug("doGetAuthorizationInfo for login password id: {}", loginPasswordId.getLoginPasswordId());
                
                
                List<Role> roles = dao.findRolesByUserLoginPasswordId(loginPasswordId.getLoginPasswordId());
                ArrayList<UUID> roleIds = new ArrayList<>();
                for (Role role : roles) {
                    log.debug("doGetAuthorizationInfo found role: {}", role.getRoleName());
                    roleIds.add(role.getId());
                    authzInfo.addRole(role.getRoleName());
                }
                if (!roleIds.isEmpty()) {
                    List<RolePermission> permissions = dao.findRolePermissionsByPasswordRoleIds(roleIds);
                    for (RolePermission permission : permissions) {
                        log.debug("doGetAuthorizationInfo found permission: {} {} {}", permission.getPermitDomain(), permission.getPermitAction(), permission.getPermitSelection());
                        authzInfo.addStringPermission(String.format("%s:%s:%s", permission.getPermitDomain(), permission.getPermitAction(), permission.getPermitSelection()));
                    }
                }
                
            }
        } catch (Exception e) {
            log.debug("doGetAuthorizationInfo error", e);
            throw new AuthenticationException("Internal server error", e); // TODO: i18n
        }

        return authzInfo;
    }
    
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        if (username == null) {
            log.debug("doGetAuthenticationInfo null username");
            throw new AccountException("Username must be provided");
        }
        log.debug("doGetAuthenticationInfo for username {}", username);
        UserLoginPassword userLoginPassword = null;
        try (LoginDAO dao = MyJdbi.authz()) {
            userLoginPassword = dao.findUserLoginPasswordByUsername(username);
        } catch (Exception e) {
            log.debug("doGetAuthenticationInfo error", e);
            throw new AuthenticationException("Internal server error", e); // TODO: i18n
        }
        if (userLoginPassword == null) {
            return null;
        }
        log.debug("doGetAuthenticationInfo found user login password id {}", userLoginPassword.getId());
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add(new UserId(userLoginPassword.getUserId()), getName());
        principals.add(new Username(username), getName());
        principals.add(new LoginPasswordId(userLoginPassword.getUserId(), userLoginPassword.getId()), getName());

        PasswordAuthenticationInfo info = new PasswordAuthenticationInfo();
        info.setPrincipals(principals);
        info.setCredentials(userLoginPassword);

        return info;
    }
}
