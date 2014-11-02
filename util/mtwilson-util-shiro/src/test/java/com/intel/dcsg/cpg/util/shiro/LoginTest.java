/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util.shiro;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class LoginTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginTest.class);
    
    @BeforeClass
    public static void init() {
//        Login.superuser();
        Login.user("document:read");
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
    
    @Test(expected=UnauthorizedException.class)
    public void testWritePermission() {
        // if you try to access document manager readDocument or writeDocument without logging in you will get an AuthorizationException
        DocumentManager manager = new DocumentManager();
        manager.writeDocument("a", "text");
    }

    @Test
    public void testReadPermission() {
        // if you try to access document manager readDocument or writeDocument without logging in you will get an AuthorizationException
        DocumentManager manager = new DocumentManager();
        manager.readDocument("a");
    }
    
}
