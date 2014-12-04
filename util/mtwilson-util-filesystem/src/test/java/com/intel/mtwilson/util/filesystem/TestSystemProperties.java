/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.filesystem;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class TestSystemProperties {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestSystemProperties.class);

    @Test
    public void testSetProperty() {
        String key = "test.property25123512";
        String test = System.getProperty(key, "defaultValue");
        assertEquals("defaultValue", test);
        System.setProperty(key, "customValue");
        String test2 = System.getProperty(key, "defaultValue2");
        assertEquals("customValue", test2);
    }
}
