/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import com.intel.mtwilson.text.transform.AllCapsNamingStrategy;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class KeyTransformerConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeyTransformerConfigurationTest.class);

    @Test
    public void testKeyTransformer() {
        PropertiesConfiguration env = new PropertiesConfiguration();
        env.set("FRUIT_COLOR", "red");
        env.set("FRUIT_SHAPE", "circle");
        KeyTransformerConfiguration config = new KeyTransformerConfiguration(new AllCapsNamingStrategy(), env);
        assertEquals("red", config.get("fruit.color"));
        assertEquals("circle", config.get("fruit.shape"));
    }
}
