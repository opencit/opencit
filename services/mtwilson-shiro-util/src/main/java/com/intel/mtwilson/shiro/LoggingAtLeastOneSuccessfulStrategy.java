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

/**
 *
 * @author jbuhacoff
 */
public class LoggingAtLeastOneSuccessfulStrategy extends AtLeastOneSuccessfulStrategy {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingAtLeastOneSuccessfulStrategy.class);

    @Override
    public AuthenticationInfo afterAllAttempts(AuthenticationToken token, AuthenticationInfo aggregate) throws AuthenticationException {
        log.debug("afterAllAttempts {}", token.getPrincipal().getClass().getName());
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
