/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.junit.Test;
import org.junit.BeforeClass;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.util.Factory;

/**
 *
 * @author jbuhacoff
 */
public class ShiroTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShiroTest.class);

    @BeforeClass
    public static void login() throws Exception {
        // define the known users with passwords, roles, and permissions
        Ini ini = new Ini();
//        ini.addSection("users");
//        ini.addSection("roles");
        ini.setSectionProperty("users", "alice", "password,document_reader");
        ini.setSectionProperty("users", "bob", "password,document_writer");
        ini.setSectionProperty("roles", "document_reader", "document:read");
        ini.setSectionProperty("roles", "document_writer", "document:read,document:write");
        Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory(ini);
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
        // login
        UsernamePasswordToken loginToken = new UsernamePasswordToken("alice", "password");
        SecurityUtils.getSubject().login(loginToken);            
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

    @Test(expected=UnauthorizedException.class) // Subject does not have permission [document:write]
    public void testUnauthorizedPermission() {
        // if you try to access document manager readDocument or writeDocument without logging in you will get an AuthorizationException
        DocumentManager manager = new DocumentManager();
        manager.writeDocument("a", "text");
    }

}
