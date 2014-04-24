/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import com.intel.dcsg.cpg.util.shiro.Login;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author jbuhacoff
 */
public class JunitSuperuserTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JunitSuperuserTest.class);

    @BeforeClass
    public static void login() throws Exception {
        Login.superuser();
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
