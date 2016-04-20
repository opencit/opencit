/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.file;

import com.intel.mtwilson.shiro.Username;
import com.intel.mtwilson.shiro.authc.password.HashedPassword;
import com.intel.mtwilson.shiro.authc.password.PasswordAuthenticationInfo;
import com.intel.mtwilson.shiro.file.model.UserPassword;
import com.intel.mtwilson.shiro.file.model.UserPermission;
import java.io.File;
import java.util.Collection;
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
 * 
 * @author jbuhacoff
 */
public class FilePasswordRealm extends AuthorizingRealm {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FilePasswordRealm.class);
    
    private String userFilePath;
    private String permissionFilePath;

    public void setUserFilePath(String userFilePath) {
        this.userFilePath = userFilePath;
    }

    public void setPermissionFilePath(String permissionFilePath) {
        this.permissionFilePath = permissionFilePath;
    }
    
    
    
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
        try {
            LoginDAO dao = new LoginDAO(new File(userFilePath), new File(permissionFilePath));
            Collection<Username> usernames = pc.byType(Username.class);
            for(Username username : usernames) {
                log.debug("doGetAuthorizationInfo for username: {}", username.getUsername());
                List<UserPermission> permissions = dao.getPermissions(username.getUsername());
                for(UserPermission permission : permissions) {
                    authzInfo.addStringPermission(permission.toString());
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
        UserPassword userLoginPassword;
        try {
            LoginDAO dao = new LoginDAO(new File(userFilePath), new File(permissionFilePath));
            userLoginPassword = dao.findUserByName(username);
            if (userLoginPassword == null) {
                return null;
            }
        } catch (Exception e) {
            log.debug("doGetAuthenticationInfo error", e);
            throw new AuthenticationException("Internal server error", e); 
        }
        log.debug("doGetAuthenticationInfo found user {}", userLoginPassword.getUsername());
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add(new Username(username), getName());

        PasswordAuthenticationInfo info = new PasswordAuthenticationInfo();
        info.setPrincipals(principals);
        info.setCredentials(toHashedPassword(userLoginPassword));

        return info;
    }

    private HashedPassword toHashedPassword(UserPassword userLoginPassword) {
        HashedPassword hashedPassword = new HashedPassword();
        hashedPassword.setAlgorithm(userLoginPassword.getAlgorithm());
        hashedPassword.setSalt(userLoginPassword.getSalt());
        hashedPassword.setIterations(userLoginPassword.getIterations());
        hashedPassword.setPasswordHash(userLoginPassword.getPasswordHash());
        return hashedPassword;
    }

}
