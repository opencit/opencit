/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.x509;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mtwilson.shiro.*;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.shiro.jdbi.model.Role;
import com.intel.mtwilson.shiro.jdbi.model.RolePermission;
import com.intel.mtwilson.shiro.jdbi.model.UserLoginCertificate;
import com.intel.mtwilson.shiro.jdbi.model.User;
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
 *
 * @author jbuhacoff
 */
public class JdbcCertificateRealm extends AuthorizingRealm {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JdbcCertificateRealm.class);
    
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof X509AuthenticationToken;
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
            
            Collection<LoginCertificateId> loginCertificateIds = pc.byType(LoginCertificateId.class);
            for (LoginCertificateId loginCertificateId : loginCertificateIds) {
                log.debug("doGetAuthorizationInfo for login password id: {}", loginCertificateId.getLoginCertificateId());
                
                
                List<Role> roles = dao.findRolesByUserLoginCertificateId(loginCertificateId.getLoginCertificateId());
                ArrayList<UUID> roleIds = new ArrayList<>();
                for (Role role : roles) {
                    log.debug("doGetAuthorizationInfo found role: {}", role.getRoleName());
                    roleIds.add(role.getId());
                    authzInfo.addRole(role.getRoleName());
                }
                if (!roleIds.isEmpty()) {
                    List<RolePermission> permissions = dao.findRolePermissionsByCertificateRoleIds(roleIds);
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
        X509AuthenticationToken xToken = (X509AuthenticationToken) token;
        UserLoginCertificate userLoginCertificate = null;
        User user = null;
        if( xToken.getPrincipal() instanceof Fingerprint ) {
            Fingerprint fingerprint = (Fingerprint)xToken.getPrincipal();
            log.debug("doGetAuthenticationInfo for fingerprint {}", fingerprint.getHex());
            try (LoginDAO dao = MyJdbi.authz()) {
                if( Sha256Digest.isValid(fingerprint.getBytes())) {
                    userLoginCertificate = dao.findUserLoginCertificateBySha256(fingerprint.getBytes()); 
                }
                else if( Sha1Digest.isValid(fingerprint.getBytes())) {
                    userLoginCertificate = dao.findUserLoginCertificateBySha1(fingerprint.getBytes()); 
                }
                else {
                    log.error("Unsupported digest length {}", fingerprint.getBytes().length);
                }
                if(userLoginCertificate != null ) {
                    user = dao.findUserById(userLoginCertificate.getUserId());
                }
    //            xToken.
    //            userLoginCertificate = dao.findUserLoginCertificateByUsername(username);
            } catch (Exception e) {
                log.debug("doGetAuthenticationInfo error", e);
                throw new AuthenticationException("Internal server error", e); // TODO: i18n
            }
        }
        if (userLoginCertificate == null || user == null) {
            return null;
        }
        
        
        log.debug("doGetAuthenticationInfo found user login certificate id {}", userLoginCertificate.getId());
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add(new UserId(userLoginCertificate.getUserId()), getName());
        principals.add(new Username(user.getUsername()), getName());
        principals.add(new LoginCertificateId(userLoginCertificate.getUserId(), userLoginCertificate.getId()), getName());
        // should we add the Fingerprint principal?  or is it enough to use LoginCertificateId ?
        X509AuthenticationInfo info = new X509AuthenticationInfo();
        info.setPrincipals(principals);
        info.setCredentials(userLoginCertificate);

        return info;
    }
}
