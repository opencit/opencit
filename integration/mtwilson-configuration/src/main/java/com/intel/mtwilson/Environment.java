/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.dcsg.cpg.configuration.EnvironmentConfiguration;
import com.intel.dcsg.cpg.configuration.PrefixConfiguration;
import java.util.Set;

/**
 * Convenience for obtaining values of application environment variables
 * of the form PREFIX_VARNAME where default prefix is "MTWILSON_" or 
 * another value specified by the system property mtwilson.environment.prefix
 * 
 * To get "raw" environment variables without an automatic prefix, 
 * use {@code System.getenv}.
 * 
 * @author jbuhacoff
 */
public class Environment {
    
    private static final PrefixConfiguration environment = new PrefixConfiguration(new EnvironmentConfiguration(), System.getProperty("mtwilson.environment.prefix", "MTWILSON_"));

//    public static Configuration getConfiguration() { return environment; }
    
    /**
     * 
     * @return the prefix used to filter available environment variables 
     */
    public static String prefix() {
        return environment.getPrefix();
    }
    
    /**
     * 
     * @return set of all available environment variables (ones that begin with the prefix)
     */
    public static Set<String> keys() {
        return environment.keys();
    }
    
    /**
     * 
     * @param key
     * @return value of corresponding environment variable, or null if it is not set
     */
    public static String get(String key) {
        return environment.get(key);
    }
    
    /**
     * 
     * @param key 
     * @param defaultValue
     * @return value of corresponding environment variable, or defaultValue if it is not set
     */
    public static String get(String key, String defaultValue) {
        return environment.get(key, defaultValue);
    }
}
