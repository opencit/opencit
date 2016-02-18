/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mtwilson.shiro.*;
import com.intel.dcsg.cpg.net.NetUtils;
import com.intel.dcsg.cpg.rfc822.Rfc822Date;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.shiro.authc.x509.Credential;
import com.intel.mtwilson.shiro.authc.x509.Fingerprint;
import com.intel.mtwilson.shiro.authc.x509.LoginCertificateId;
import com.intel.mtwilson.shiro.authc.x509.X509AuthenticationInfo;
import com.intel.mtwilson.shiro.authc.x509.X509AuthenticationToken;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import java.net.SocketException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
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
 * Example X509 Authorization header:
 * <pre>
 * Authorization: X509 fingerprint="H1PPayM0FcOHtdUhHOuZlBaeynVrYl9yJV3JqqAsMHc=", headers="X-Nonce,Date", algorithm="SHA256withRSA", signature="BRlDe76PQLkKWgG982Su+/wPdDRIOa6eKdstpxI6tPWKUod8H46yqaPSyapjufnOPuknJ6IXOBY42xSjD/Dl4Les/JciodI/4BGuThMZDPRft+hnijM2A876OX4L60J/pMW+1/s6Ar/zTofK0d4oOpGyyu2QdZ8pGMlRhUXejGEChRjBKYMpf0Z+EsTaRQqhya18G/NeqJufjx571X50JJE4UaX3MIkpiSsX+em9sCtMayvUBzfnaQDZcRG5/DDTnfsbPQaMhhOtpZ9W4xJYWH1/6BwWVT+PLRz0Ztpq5atDhZ82XEk92nwXY9hYJ/VpoBb3ZhCZUNIildEceW/TiQ=="
 * </pre>
 * 
 * @author jbuhacoff
 */
public class JdbcCertificateRealm extends AuthorizingRealm {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JdbcCertificateRealm.class);
    private String localAddress;

    public JdbcCertificateRealm() {
        super();
        try {
            List<String> localAddresses = NetUtils.getNetworkAddressList();
            if( localAddresses.isEmpty() ) {
                localAddress = String.format("default-%s", getRandomNodeId());
            }
            else {
                localAddress = localAddresses.get(0);
            }
        }
        catch(SocketException e) {
            log.debug("Cannot determine local network address", e);
            localAddress = String.format("unknown-%s", getRandomNodeId());
        }
    }
    
    private String getRandomNodeId() {
        SecureRandom random = new SecureRandom();
        String id = String.valueOf(random.nextInt());
        return id;
    }
    
    
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
            log.debug("doGetAuthorizationInfo got DAO instance: {}", dao);
            Collection<LoginCertificateId> loginCertificateIds = pc.byType(LoginCertificateId.class);
            for (LoginCertificateId loginCertificateId : loginCertificateIds) {
                log.debug("doGetAuthorizationInfo for login certificate id: {}", loginCertificateId.getLoginCertificateId());
                
                
                List<Role> roles = dao.findRolesByUserLoginCertificateId(loginCertificateId.getLoginCertificateId());
                HashSet<String> roleIds = new HashSet<>();
                for (Role role : roles) {
                    log.debug("doGetAuthorizationInfo found role: {}", role.getRoleName());
                    roleIds.add(role.getId().toString());
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
            throw new AuthenticationException("Internal server error", e); 
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
                // anti-replay protection: check the database to see if we have already received a request with this signature
                // it should not affect legitimate clients because if they send the same logical request twice, the timestamp and
                // nonce would be different which would result in a different digest and signature and thus would still have be unique
                Credential credential = (Credential)xToken.getCredentials();
                RequestLogEntry requestLogEntry = new RequestLogEntry();
                requestLogEntry.setDigest(Base64.encodeBase64String(credential.getDigest()));
                requestLogEntry.setSource(xToken.getHost());
                requestLogEntry.setInstance(localAddress);
                requestLogEntry.setReceived(new Date());
                requestLogEntry.setContent(xToken.getSignatureInput().toString());
                try {
                    // first, look at the date header in the request and compare to our current time; if it's earlier than
                    // the earliest entry in the request log, then we have to reject it in order to prevent replay attacks
                    // of messages that we already deleted from the request log.
                    // clients MUST include a date header in the request and cover it with the signature.
                    // clients SHOULD synchronize their clocks (at least for the requests) with our server clock to avoid rejecting messages unnecessarily
                    if( xToken.getSignatureInput().headers == null || !xToken.getSignatureInput().headers.containsKey("Date") ) {
                        log.debug("request does not include date header");
                        return null;
                    }
                    // second, check if the user has specified an expiration time for the request and enforce it
                    if( xToken.getSignatureInput().headers.containsKey("Expires") ) {
                        Date expires = Rfc822Date.parse(xToken.getSignatureInput().headers.get("Expires"));
                        if( requestLogEntry.getReceived().after(expires) ) {
                            log.debug("request has expired; must be rejected");
                            return null;
                        }
                    }
                    // third, ensure that the request is not earlier than the start of our anti-replay protection window (represented by the earliest request received in the request log) 
                    Date requestDate = Rfc822Date.parse(xToken.getSignatureInput().headers.get("Date"));
                    RequestLogEntry earliest = dao.findRequestLogEntryByEarliestDate();
                    if( earliest != null && requestDate.before(earliest.getReceived()) ) {
                        log.debug("request date is before anti-replay window; must be rejected");
                        return null;
                    }
                    // fourth, try to insert the request into the log - it will fail if there is already a request with the same digest
                    log.debug("inserting request log entry with digest {} from source {} received at {} by {}", requestLogEntry.getDigest(), requestLogEntry.getSource(), requestLogEntry.getReceived(), requestLogEntry.getInstance());
                    dao.insertRequestLogEntry(requestLogEntry);
                    // so at this point, we have inserted a new request into the request log 
                    // and it's protected from replay because the request digest is in the log so
                    // if a duplicate request comes in then it won't be able to insert (above)
                }
                catch(Exception e) {
                    log.debug("Cannot insert request log entry", e); // probably a duplicate, but could also be database connection issue
                    return null;
                }
                
                if( Sha256Digest.isValid(fingerprint.getBytes())) {
                    userLoginCertificate = dao.findUserLoginCertificateBySha256(fingerprint.getBytes()); 
                }
                else if( Sha1Digest.isValid(fingerprint.getBytes())) {
                    userLoginCertificate = dao.findUserLoginCertificateBySha1(fingerprint.getBytes()); 
                }
                else {
                    log.error("Unsupported digest length {}", fingerprint.getBytes().length);
                }
                if(userLoginCertificate != null && userLoginCertificate.isEnabled() ) {
                    user = dao.findUserById(userLoginCertificate.getUserId());
                }
    //            xToken.
    //            userLoginCertificate = dao.findUserLoginCertificateByUsername(username);
            } catch (Exception e) {
                log.debug("doGetAuthenticationInfo error", e);
                throw new AuthenticationException("Internal server error", e); 
            }
        }
        if (userLoginCertificate == null || user == null) {
            return null;
        }
        
        
        log.debug("doGetAuthenticationInfo found user login certificate id {}", userLoginCertificate.getId());
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add(new UserId(userLoginCertificate.getUserId()), getName());
        principals.add(new Username(user.getUsername()), getName());
        principals.add(new LoginCertificateId(user.getUsername(), userLoginCertificate.getUserId(), userLoginCertificate.getId()), getName());
        // should we add the Fingerprint principal?  or is it enough to use LoginCertificateId ?
        X509AuthenticationInfo info = new X509AuthenticationInfo();
        info.setPrincipals(principals);
        try {
            X509Certificate decodeDerCertificate = X509Util.decodeDerCertificate(userLoginCertificate.getCertificate());
            log.trace("Decoded DER certificate public key is {}", decodeDerCertificate.getPublicKey().toString());
            info.setCredentials(X509Util.decodeDerCertificate(userLoginCertificate.getCertificate()));
        }
        catch(CertificateException e) {
            throw new AuthenticationException("Invalid certificate", e); 
        }

        return info;
    }
        
}
