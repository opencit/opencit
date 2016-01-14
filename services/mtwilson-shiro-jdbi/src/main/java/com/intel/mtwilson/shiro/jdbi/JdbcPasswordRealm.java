/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.shiro.UserId;
import com.intel.mtwilson.shiro.Username;
import com.intel.mtwilson.shiro.UsernameWithPermissions;
import com.intel.mtwilson.crypto.password.HashedPassword;
import com.intel.mtwilson.shiro.authc.password.LoginPasswordId;
import com.intel.mtwilson.shiro.authc.password.PasswordAuthenticationInfo;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * @author jbuhacoff
 */
public class JdbcPasswordRealm extends AuthorizingRealm {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JdbcPasswordRealm.class);

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
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
        try (LoginDAO dao = MyJdbi.authz()) {
            
            Collection<LoginPasswordId> loginPasswordIds = pc.byType(LoginPasswordId.class);
            for (LoginPasswordId loginPasswordId : loginPasswordIds) {
                log.debug("doGetAuthorizationInfo for login password id: {}", loginPasswordId.getLoginPasswordId());
                
                
                List<Role> roles = dao.findRolesByUserLoginPasswordId(loginPasswordId.getLoginPasswordId());
                HashSet<String> roleIds = new HashSet<>();
                for (Role role : roles) {
                    log.debug("doGetAuthorizationInfo found role: {}", role.getRoleName());
                    roleIds.add(role.getId().toString());
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
            throw new AuthenticationException("Internal server error", e); 
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
        UserLoginPassword userLoginPassword;
        User user;
        UsernameWithPermissions usernameWithPermissions = null;
        try (LoginDAO dao = MyJdbi.authz()) {
            userLoginPassword = dao.findUserLoginPasswordByUsernameEnabled(username, true);
            if( userLoginPassword != null && userLoginPassword.isEnabled() ) {
                user = dao.findUserById(userLoginPassword.getUserId());
                
                // now get the permissions and associate with the username
                HashSet<String> stringPermissions = new HashSet<>();
                List<Role> roles = dao.findRolesByUserLoginPasswordId(userLoginPassword.getId());
                if (!roles.isEmpty()) {
                    HashSet<String> roleIds = new HashSet<>();
                    for(Role role : roles) {
                        roleIds.add(role.getId().toString());
                    }
                    List<RolePermission> permissions = dao.findRolePermissionsByPasswordRoleIds(roleIds);
                    for (RolePermission permission : permissions) {
                        log.debug("doGetAuthorizationInfo found permission: {} {} {}", permission.getPermitDomain(), permission.getPermitAction(), permission.getPermitSelection());
                        stringPermissions.add(String.format("%s:%s:%s", permission.getPermitDomain(), permission.getPermitAction(), permission.getPermitSelection()));
                    }
                }
                
                usernameWithPermissions = new UsernameWithPermissions(user.getUsername(), stringPermissions);
                
                
            } else {
                user = null;
            }
        } catch (Exception e) {
            log.debug("doGetAuthenticationInfo error", e);
            throw new AuthenticationException("Internal server error", e);
        }
        if (userLoginPassword == null || user == null ) {
            return null;
        }

        log.debug("doGetAuthenticationInfo found user login password id {}", userLoginPassword.getId());
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add(new UserId(userLoginPassword.getUserId()), getName());
//        principals.add(new Username(username), getName());
        if( usernameWithPermissions != null ) {
            principals.add(usernameWithPermissions, getName());
        }
        principals.add(new LoginPasswordId(user.getUsername(), userLoginPassword.getUserId(), userLoginPassword.getId()), getName());

        //HashedPassword hashedPassword = new HashedPassword();
        
        PasswordAuthenticationInfo info = new PasswordAuthenticationInfo();
        info.setPrincipals(principals);
        info.setCredentials(toHashedPassword(userLoginPassword));

        return info;
    }
    
    private HashedPassword toHashedPassword(UserLoginPassword userLoginPassword) {
        HashedPassword hashedPassword = new HashedPassword();
        hashedPassword.setAlgorithm(userLoginPassword.getAlgorithm());
        hashedPassword.setSalt(userLoginPassword.getSalt());
        hashedPassword.setIterations(userLoginPassword.getIterations());
        hashedPassword.setPasswordHash(userLoginPassword.getPasswordHash());
        return hashedPassword;
    }
}
