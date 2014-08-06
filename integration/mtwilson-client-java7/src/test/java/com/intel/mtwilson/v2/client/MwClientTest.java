/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.client;

import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import java.util.Properties;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class MwClientTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwClientTest.class);
    
    public static class FeatureA {
        public void doA() { log.debug("A"); }
    }
    public static class FeatureB {
        public void doB() { log.debug("B"); }
    }
    
    /*@Test
    public void testMwClientRegistry() {
        MwClientHolder mwClient = new MwClientHolder(new Properties());
        FeatureA a = new FeatureA();
        mwClient.set(a);
        FeatureA a2 = mwClient.get(FeatureA.class);
        a2.doA();
        FeatureB b = new FeatureB();
        mwClient.set(FeatureB.class, b);
        FeatureB b2 = mwClient.get(FeatureB.class);
        b2.doB();
    }*/
}
