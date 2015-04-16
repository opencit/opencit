/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import com.intel.dcsg.cpg.configuration.AllCapsConfiguration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class AllCapsConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AllCapsConfigurationTest.class);

    @Test
    public void testAllCapsConfiguration() {
        PropertiesConfiguration p = new PropertiesConfiguration();
        p.set("foo", "bar");
        p.set("foo28", "alksdjfa");
        p.set("WOOOK", "aaaaaa");
        p.set("UUUUX", "BBBBBB");
        for(String key : p.keys()) {
            log.debug("original key: {} value: {}", key, p.get(key));
        }
        AllCapsConfiguration a = new AllCapsConfiguration(p);
        for(String key : a.keys()) {
            log.debug("allcaps key: {} value: {}", key, a.get(key));
        }
        assertEquals("aaaaaa", a.get("woook"));
        assertEquals("BBBBBB", a.get("uuuux"));
        assertNull(a.get("foo"));
        assertNull(a.get("foo28"));
        
        
    }
    
    @Test
    public void testAllCapsRegex() {
        assertTrue("A0_XZ".matches("^[A-Z0-9_]*$"));
        assertFalse("a0_xz".matches("^[A-Z0-9_]*$"));
        assertFalse("a0.xz".matches("^[A-Z0-9_]*$"));
    }
}
