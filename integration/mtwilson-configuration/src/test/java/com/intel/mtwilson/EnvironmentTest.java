/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class EnvironmentTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    
    @Test
    public void testFilesystem() {
        System.setProperty("mtwilson.environment.prefix", "MTWILSON_");
        
        log.debug("JAVA_HOME = {}", System.getenv("JAVA_HOME"));
        log.debug("MTWILSON_HOME = {}", System.getenv("MTWILSON_HOME"));
        log.debug("HOME = {}", Environment.get("HOME"));
        
        for(String key : Environment.keys()) {
            log.debug("key {} value {}", key, Environment.get(key));
        }
    }
}
