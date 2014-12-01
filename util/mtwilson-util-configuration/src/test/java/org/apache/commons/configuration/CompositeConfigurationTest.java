/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package org.apache.commons.configuration;

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
        c1.setProperty("fruit", "apple");
        c2.setProperty("fruit", "banana");
        c1.setProperty("number", 1);
        c2.setProperty("boolean", false);
        CompositeConfiguration cc = new CompositeConfiguration();
        cc.addConfiguration(c1);
        cc.addConfiguration(c2);
        assertEquals("apple", cc.getString("fruit"));
        assertEquals(Integer.valueOf(1), cc.getInteger("number", null));
        assertEquals(false, cc.getBoolean("boolean"));
        assertFalse(cc.containsKey("missing"));
        // now what happens when a property is cleared from compostite configuration? it clears FROM ALL OF THEM
        cc.clearProperty("fruit"); 
        assertNull(c1.getString("fruit"));
        assertNull(c2.getString("fruit"));
    }
    
    @Test
    public void testCompositeConfigurationWithInMemoryPrimary() {
        PropertiesConfiguration c1 = new PropertiesConfiguration();
        PropertiesConfiguration c2 = new PropertiesConfiguration();
        c1.setProperty("fruit", "apple");
        c2.setProperty("fruit", "banana");
        c1.setProperty("number", 1);
        c2.setProperty("boolean", false);
        CompositeConfiguration cc = new CompositeConfiguration();
        cc.addConfiguration(c1, true);
        cc.addConfiguration(c2);
        assertEquals("apple", cc.getString("fruit"));
        assertEquals(Integer.valueOf(1), cc.getInteger("number", null));
        assertEquals(false, cc.getBoolean("boolean"));
        assertFalse(cc.containsKey("missing"));
        // now what happens when a property is cleared from compostite configuration? it clears FROM ALL OF THEM, EVEN WHEN C1 IS DESIGNATED AS THE IN-MEMORY / WRITE-TO CONFIGURATION
        cc.clearProperty("fruit"); 
        assertNull(c1.getString("fruit"));
        assertNull(c2.getString("fruit"));
    }
    
}
