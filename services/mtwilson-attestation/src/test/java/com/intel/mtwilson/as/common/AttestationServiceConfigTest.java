package com.intel.mtwilson.as.common;


import com.intel.mountwilson.as.common.ASConfig;
import org.apache.commons.configuration.Configuration;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class AttestationServiceConfigTest {

    /**
     * Demonstrates how to use the ASConfig class
     */
    @Test
    public void testLoadASConfig() {
        Configuration config = ASConfig.getConfiguration();
        // test for existence of a configuration key
        assertTrue(config.containsKey("mountwilson.as.db.user"));
        
        // display current settings, but do not assert them because they will be different on various developer machines
        System.out.println("mountwilson.as.db.user: "+config.getString("mountwilson.as.db.user"));
        System.out.println("com.intel.mountwilson.as.home: "+config.getString("com.intel.mountwilson.as.home"));
    }
}
