/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.shiro;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.junit.Test;
import com.intel.mtwilson.test.UnitTest;

/**
 *
 * @author jbuhacoff
 */
public class ShiroUnitTest extends UnitTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShiroUnitTest.class);
    
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
