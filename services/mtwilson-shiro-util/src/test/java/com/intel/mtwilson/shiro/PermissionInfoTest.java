/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class PermissionInfoTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PermissionInfoTest.class);

    @Test
    public void testPermissionInfoToString() {
        PermissionInfo info = new PermissionInfo("domain",null,null);
        log.debug("info: {}", info);
        assertEquals("domain", info.toString());
    }
    
    @Test
    public void testStringFormat() {
        String format = String.format("value '%s' null '%s'", "value", null);
        log.debug("format: {}", format);
        assertEquals("value 'value' null 'null'", format);
        
        PermissionInfo info = new PermissionInfo("domain","action",null);
        String format2 = String.format("%s", info); // testing if we can just use info here or if we have to write info.toString() ... just info is fine,  toString() gets called automatically
        assertEquals("domain:action", format2);
    }
}
