/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import java.util.Collection;
import java.util.Iterator;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * 
 * @author jbuhacoff
 */
public class ShiroUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShiroUtil.class);
    
    public static boolean subjectUsernameEquals(String username) {
        PrincipalCollection principalCollection = SecurityUtils.getSubject().getPrincipals();
        Collection<Username> clientUsernameCollection = principalCollection.byType(Username.class);
        Iterator<Username> it = clientUsernameCollection.iterator();
        if( it.hasNext() ) {
            Username clientUsername = it.next();
            log.debug("client username: {}", clientUsername.getUsername()); // for example, "admin"  matches portalUser.getUsername() == "admin"
            if( clientUsername.getUsername().equals(username) ) {
                return true;
            }
        }
        return false;
    }
    
    public static String subjectUsername() {
        PrincipalCollection principalCollection = SecurityUtils.getSubject().getPrincipals();
        Collection<Username> clientUsernameCollection = principalCollection.byType(Username.class);
        Iterator<Username> it = clientUsernameCollection.iterator();
        if( it.hasNext() ) {
            Username clientUsername = it.next();
            log.debug("client username: {}", clientUsername.getUsername()); // for example, "admin"
            return clientUsername.getUsername();
        }
        return null;
    }
}
