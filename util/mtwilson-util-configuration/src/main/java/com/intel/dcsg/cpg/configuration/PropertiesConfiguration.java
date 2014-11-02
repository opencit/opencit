/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.Properties;

/**
 * If the property is null (missing) or has an empty string value, the
 *  getXYZ methods will return null with the exception of getString which
 * will return either null or empty string.
 * 
 * @author jbuhacoff
 */
public class PropertiesConfiguration extends MutableStringConfiguration {
    private Properties properties;
    
    public PropertiesConfiguration() {
        this(new Properties());
    }
    public PropertiesConfiguration(Properties properties) {
        super();
        this.properties = properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
        
    public Properties getProperties() { return properties; }

    @Override
    protected String get(String key) {
        return properties.getProperty(key);
    }

    @Override
    protected void set(String key, String value) {
        if( value == null ) {
            properties.remove(key);
        }
        else {
            properties.setProperty(key, value);
        }
    }
}
