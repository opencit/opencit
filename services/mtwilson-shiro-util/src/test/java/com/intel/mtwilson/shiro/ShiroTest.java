/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import com.intel.mtwilson.My;
import java.io.IOException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ShiroTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShiroTest.class);
    
    @BeforeClass
    public static void initSecurityManager() throws IOException {
        com.intel.mtwilson.shiro.env.JunitEnvironment.init();
        login();
    }
    
    public static void login() throws IOException {
        Subject currentUser = SecurityUtils.getSubject();        
//        if( !currentUser.isAuthenticated() ) { // shouldn't need this because we have @RequiresGuest annotation...
            log.debug("authenticating...");
            // for this junit test we're using mtwilson.api.username and mtwilson.api.password properties from  mtwilson.properties on the local system, c:/mtwilson/configuration/mtwilson.properties is default location on windows 
        UsernamePasswordToken loginToken = new UsernamePasswordToken(My.configuration().getKeystoreUsername(), My.configuration().getKeystorePassword());
//            UsernamePasswordToken token = new UsernamePasswordToken("root", "root"); // guest doesn't need a password
            loginToken.setRememberMe(false); // we could pass in a parameter with the form but we don't need this
            currentUser.login(loginToken); // throws UnknownAccountException , IncorrectCredentialsException , LockedAccountException , other specific exceptions, and AuthenticationException 
        
    }
    
    protected static class DocumentManager {
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DocumentManager.class);
        
        @RequiresPermissions("document:write")
        public void writeDocument(String id, String text) {
            log.debug("writeDocument({},...) ok", id);
        }
        
        @RequiresPermissions("document:read")
        public String readDocument(String id) {
            log.debug("readDocument({}) ok", id);
            return "hello";
        }
    }
    
    @Test
    public void testPermission() {
        // if you try to access document manager readDocument or writeDocument without logging in you will get an AuthorizationException
        DocumentManager manager = new DocumentManager();
        manager.readDocument("a");
    }
}
