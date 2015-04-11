/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.token;

import com.intel.mtwilson.shiro.UsernameWithPermissions;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AccountException;
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
 * 
 * @author jbuhacoff
 */
public class MemoryTokenRealm extends AuthorizingRealm {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemoryTokenRealm.class);
    private static volatile MemoryTokenDatabase database = new MemoryTokenDatabase();

    public static MemoryTokenDatabase getDatabase() {
        return database;
    }
    
    
    
    public MemoryTokenRealm() {
        super();
    }
    
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof TokenAuthenticationToken;
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
            Collection<Token> subjects = pc.byType(Token.class);
            for(Token subject : subjects) {
                log.debug("doGetAuthorizationInfo for token: {}", subject.getValue());
                TokenRecord existing = database.findByTokenValue(subject.getValue());
                if( existing == null ) {
                    log.debug("doGetAuthorizationInfo token not found: {}", subject.getValue());
                    continue;
                }
                log.debug("doGetAuthorizationInfo found permissions: {}", existing.getPermissions());
                for(String permission : existing.getPermissions() ) {
                    log.debug("doGetAuthorizationInfo adding permision '{}'", permission);
                    authzInfo.addStringPermission(permission);
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
        TokenAuthenticationToken subjectToken = (TokenAuthenticationToken) token;
        String tokenValue = subjectToken.getToken().getValue();
        if (tokenValue == null) {
            log.debug("doGetAuthenticationInfo null token value");
            throw new AccountException("Token must be provided");
        }
        log.debug("doGetAuthenticationInfo for token {}", tokenValue);
        TokenCredential tokenCredential;
        try {
            //TokenRecord existing = database.findByTokenValue(tokenValue);
            tokenCredential = database.findCredentialByTokenValue(tokenValue);
            if( tokenCredential == null ) {
                log.debug("doGetAuthenticationInfo token value not found in database: {}", tokenValue);
                return null;
            }
            if( !tokenCredential.getValue().equals(tokenValue) ) {
                log.debug("doGetAuthenticationInfo found record but token value does not match: {}", tokenValue);
                return null;
            }
            log.debug("doGetAuthenticationInfo found token {}", tokenCredential.getValue());
        } catch (Exception e) {
            log.debug("doGetAuthenticationInfo error", e);
            throw new AuthenticationException("Internal server error", e); 
        }
        
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add(new Token(tokenCredential.getValue()), getName());

        TokenAuthenticationInfo info = new TokenAuthenticationInfo();
        info.setPrincipals(principals);
        info.setCredentials(tokenCredential);

        return info;
    }

    public static class MemoryTokenDatabase {
        private HashMap<String,TokenRecord> map = new HashMap<>();
        
        public void add(TokenCredential credential, UsernameWithPermissions usernameWithPermissions) {
            TokenRecord existing = map.get(credential.getValue());
            if( existing != null ) {
                throw new IllegalArgumentException("Token already registered");
            }
            map.put(credential.getValue(), new TokenRecord(credential, usernameWithPermissions));
        }
        
        public TokenRecord getByTokenValue(String value) {
            TokenRecord existing = findByTokenValue(value);
            if( existing == null ) {
                throw new IllegalArgumentException("Token not found");
            }
            return existing;
        }
        
        public TokenRecord findByTokenValue(String value) {
            return map.get(value);
        }
        
        public TokenCredential findCredentialByTokenValue(String value) {
            TokenRecord existing = map.get(value);
            if( existing == null ) { return null; }
            return existing.getCredential();
        }
        
        public Set<String> findPermissionsByTokenValue(String value) {
            TokenRecord existing = map.get(value);
            if( existing == null ) { return null; }
            return existing.getPermissions();
        }
        
    }
    
    public static class TokenRecord {
        private TokenCredential credential; // the token value, validity period, and usage information
        private String username; // set only if the token corresponds to an existing user
        private Set<String> permissions; // each entry is in the format domain:action:selection format

        public TokenRecord(TokenCredential credential, UsernameWithPermissions usernameWithPermissions) {
            this.credential = credential;
            this.username = usernameWithPermissions.getUsername();
            this.permissions = usernameWithPermissions.getPermissions();
        }
        public TokenRecord(TokenCredential credential, Set<String> permissions) {
            this.credential = credential;
            this.username = null;
            this.permissions = permissions;
        }

        public TokenCredential getCredential() {
            return credential;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public String getUsername() {
            return username;
        }
        
        public boolean isAnonymous() {
            return username == null || username.isEmpty();
        }
    }
}
