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
public class CompositeConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompositeConfigurationTest.class);

    @Test
    public void testCompositeConfiguration() {
        PropertiesConfiguration c1 = new PropertiesConfiguration();
        PropertiesConfiguration c2 = new PropertiesConfiguration();
        c1.setString("fruit", "apple");
        c2.setString("fruit", "banana");
        c1.setInteger("number", 1);
        c2.setBoolean("boolean", false);
        CompositeConfiguration cc = new CompositeConfiguration(c1, c2);
        assertEquals("apple", cc.getString("fruit"));
        assertEquals(Integer.valueOf(1), cc.getInteger("number"));
        assertEquals(false, cc.getBoolean("boolean"));
        assertNull(cc.getByte("missing"));
    }
    
    @Test
    public void testMutableCompositeConfiguration() {
        PropertiesConfiguration c1 = new PropertiesConfiguration();
        PropertiesConfiguration c2 = new PropertiesConfiguration();
        c1.setString("fruit", "apple");
        c2.setString("fruit", "banana");
        c1.setInteger("number", 1);
        c2.setBoolean("boolean", false);
        PropertiesConfiguration target = new PropertiesConfiguration();
        MutableCompositeConfiguration cc = new MutableCompositeConfiguration(target, c1, c2);
        assertEquals("apple", cc.getString("fruit"));
        cc.setString("fruit", "carrot");
        assertEquals("carrot", cc.getString("fruit"));
        assertEquals("carrot", target.getString("fruit"));
        
    }
}
