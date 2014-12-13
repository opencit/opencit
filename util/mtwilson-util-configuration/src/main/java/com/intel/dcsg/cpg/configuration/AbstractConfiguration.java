/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Provides common functionality for Configuration implementations.
 * 
 * @author jbuhacoff
 */
public abstract class AbstractConfiguration implements Configuration {

    /*
    @Override
    public String get(Property property) {
        return get(property.getName(), property.getDefaultValue());
    }
    
    @Override
    public void set(Property property, String value) {
        set(property.getName(), value);
    }
    */
    
    @Override
    public String get(String key) {
        return get(key, null);
    }
    
    /**
     * Any changes to the returned Map will NOT affect the configuration
     * instance.
     * @return a Map instance with a copy of the configuration settings
     */
    public Map<String,String> toMap() {
        HashMap<String,String> map = new HashMap<>();
        for(String key : keys()) {
            String value = get(key, null);
            if( value != null ) {
                map.put(key, value);
            }  
        }
        return map;
    }
    
    /**
     * Any changes to the returned Properties will NOT affect the configuration
     * instance.
     * @return a Properties instance with a copy of the configuration settings
     */
    public Properties toProperties() {
        Properties properties = new Properties();
        for(String key : keys()) {
            String value = get(key, null);
            if( value != null ) {
                properties.setProperty(key, value);
            }  
        }
        return properties;
    }
    
}
