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
        log.debug("afterAttempt {} with realm {}: {}", token.getPrincipal().getClass().getName(), realm.getName(), realm.getClass().getName());
        return super.afterAttempt(realm, token, singleRealmInfo, aggregateInfo, t);
    }

    @Override
    public AuthenticationInfo beforeAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo aggregate) throws AuthenticationException {
        log.debug("beforeAttempt {}", token.getPrincipal().getClass().getName());
        return super.beforeAttempt(realm, token, aggregate);
    }
    
}
