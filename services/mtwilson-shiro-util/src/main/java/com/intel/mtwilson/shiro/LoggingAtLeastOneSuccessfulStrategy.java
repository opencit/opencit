/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import java.util.Collection;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * Example message when the tokens are not suitable for the authentication
 * realm: c.i.m.s.LoggingAtLeastOneSuccessfulStrategy
 * [LoggingAtLeastOneSuccessfulStrategy.java:55] beforeAttempt realm
 * jdbcPasswordRealm:com.intel.mtwilson.shiro.authc.password.JdbcPasswordRealm
 * c.i.m.s.LoggingAtLeastOneSuccessfulStrategy
 * [LoggingAtLeastOneSuccessfulStrategy.java:57] beforeAttempt token principal
 * com.intel.mtwilson.shiro.authc.x509.Fingerprint:com.intel.mtwilson.shiro.authc.x509.Fingerprint@52070253
 * c.i.m.s.LoggingAtLeastOneSuccessfulStrategy
 * [LoggingAtLeastOneSuccessfulStrategy.java:58] beforeAttempt token
 * com.intel.mtwilson.shiro.authc.x509.X509AuthenticationToken:com.intel.mtwilson.shiro.authc.x509.Credential
 * c.i.m.s.LoggingAtLeastOneSuccessfulStrategy
 * [LoggingAtLeastOneSuccessfulStrategy.java:61] beforeAttempt info
 * org.apache.shiro.authc.SimpleAuthenticationInfo
 * o.a.s.a.p.ModularRealmAuthenticator [ModularRealmAuthenticator.java:231]
 * Realm [com.intel.mtwilson.shiro.authc.password.JdbcPasswordRealm@245e4131]
 * does not support token
 * com.intel.mtwilson.shiro.authc.x509.X509AuthenticationToken@5d624607.
 * Skipping realm.
 *
 * Example message when there is an internal server error caused by a realm:
 * c.i.m.s.LoggingAtLeastOneSuccessfulStrategy
 * [LoggingAtLeastOneSuccessfulStrategy.java:36] afterAttempt realm
 * jdbcCertificateRealm:com.intel.mtwilson.shiro.authc.x509.JdbcCertificateRealm
 * c.i.m.s.LoggingAtLeastOneSuccessfulStrategy
 * [LoggingAtLeastOneSuccessfulStrategy.java:38] afterAttempt token principal
 * com.intel.mtwilson.shiro.authc.x509.Fingerprint:com.intel.mtwilson.shiro.authc.x509.Fingerprint@52070253
 * c.i.m.s.LoggingAtLeastOneSuccessfulStrategy
 * [LoggingAtLeastOneSuccessfulStrategy.java:39] afterAttempt token
 * com.intel.mtwilson.shiro.authc.x509.X509AuthenticationToken:com.intel.mtwilson.shiro.authc.x509.Credential
 * c.i.m.s.LoggingAtLeastOneSuccessfulStrategy
 * [LoggingAtLeastOneSuccessfulStrategy.java:48] afterAttempt error
 * org.apache.shiro.authc.AuthenticationException:Internal server error
 *
 *
 *
 * @author jbuhacoff
 */
public class LoggingAtLeastOneSuccessfulStrategy extends AtLeastOneSuccessfulStrategy {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingAtLeastOneSuccessfulStrategy.class);

    @Override
    public AuthenticationInfo afterAllAttempts(AuthenticationToken token, AuthenticationInfo aggregate) throws AuthenticationException {
        log.debug("afterAllAttempts {}", token.getPrincipal().getClass().getName());
        PrincipalCollection principalCollection = aggregate.getPrincipals();
        if (principalCollection != null && principalCollection.getRealmNames() != null) {
            for (String realm : principalCollection.getRealmNames()) {
                log.debug("subject has principals from realm {}", realm);
                Collection principals = principalCollection.fromRealm(realm);
                for (Object principal : principals) {
                    log.debug("subject has principal {} from realm {}", principal.getClass().getName(), realm);
                }
            }
        } else {
            log.warn("no principal collection or realm names");
        }
        return super.afterAllAttempts(token, aggregate);
    }

    @Override
    public AuthenticationInfo beforeAllAttempts(Collection<? extends Realm> realms, AuthenticationToken token) throws AuthenticationException {
        log.debug("beforeAllAttempts {} with {} realms", token.getPrincipal().getClass().getName(), realms.size());
        return super.beforeAllAttempts(realms, token);
    }

    @Override
    public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo, AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {
        log.debug("afterAttempt realm {}:{}", realm.getName(), realm.getClass().getName());
        if (token != null) {
            log.debug("afterAttempt token principal {}:{}", token.getPrincipal().getClass().getName(), token.getPrincipal().toString());
            log.debug("afterAttempt token {}:{}", token.getClass().getName(), token.getCredentials().getClass().getName());
        }
        if (singleRealmInfo != null) {
            log.debug("afterAttempt info {}", singleRealmInfo.getClass().getName());
            if (singleRealmInfo.getCredentials() != null) {
                log.debug("afterAttempt info credentials {}", singleRealmInfo.getCredentials().getClass().getName());
            }
        }
        if (t != null) {
            log.debug("afterAttempt error {}:{}", t.getClass().getName(), t.getMessage());
        }
        return super.afterAttempt(realm, token, singleRealmInfo, aggregateInfo, t);
    }

    @Override
    public AuthenticationInfo beforeAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo aggregate) throws AuthenticationException {
        log.debug("beforeAttempt realm {}:{}", realm.getName(), realm.getClass().getName());
        if (token != null) {
            log.debug("beforeAttempt token principal {}:{}", token.getPrincipal().getClass().getName(), token.getPrincipal().toString());
            log.debug("beforeAttempt token {}:{}", token.getClass().getName(), token.getCredentials().getClass().getName());
        }
        if (aggregate != null) {
            log.debug("beforeAttempt info {}", aggregate.getClass().getName());
            if (aggregate.getCredentials() != null) {
                log.debug("beforeAttempt info credentials {}", aggregate.getCredentials().getClass().getName());
            }
        }
        return super.beforeAttempt(realm, token, aggregate);
    }
}
