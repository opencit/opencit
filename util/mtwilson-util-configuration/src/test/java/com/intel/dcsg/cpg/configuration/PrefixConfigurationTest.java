/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class PrefixConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PrefixConfigurationTest.class);

    @Test
    public void testPrefixConfiguration() {
        PropertiesConfiguration p = new PropertiesConfiguration();
        p.set("foo", "aaa");
        p.set("foo.bar", "bbb");
        p.set("quuz.bar", "ccc");
        for(String key : p.keys()) {
            log.debug("original key: {} value: {}", key, p.get(key));
        }
        PrefixConfiguration a = new PrefixConfiguration(p, "foo.");
        for(String key : a.keys()) {
            log.debug("prefix key: {} value: {}", key, a.get(key));
        }
        assertEquals("bbb", a.get("bar"));
        assertEquals("bbb", a.get("foo.bar"));
        assertNull(a.get("foo"));
        assertNull(a.get("foo."));
        assertNull(a.get("quuz.bar"));
        
        
    }
}
