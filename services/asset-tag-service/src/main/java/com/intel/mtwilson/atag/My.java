/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag;

import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

/**
 * Placeholder... replace with refactored version of "My" package from Mt Wilson
 * 
 * @author jbuhacoff
 */
public class My {
    private static MyConfiguration configuration = new MyConfiguration();
    
    public static class MyConfiguration {
        private Properties properties = new Properties();
        private MapConfiguration configuration = null;        
        public MyConfiguration() {
            configuration = new MapConfiguration(properties);
        }
        public Configuration getConfiguration() { return configuration; }
        public int getServerPort() { return 17222; }
        public String getServerURL() { return String.format("http://localhost:%d", getServerPort()); }
    }
    
    public static MyConfiguration config() {
        return configuration;
    }
}
