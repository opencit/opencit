/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.filesystem;

/**
 * Represents a folder that can be configured via a system property or
 * environment variable and has a default path if neither of those is set. 
 * 
 * @author jbuhacoff
 */
public abstract class ConfigurableFolder implements Folder {
    
    abstract public String getPropertyName();
    
    abstract public String getEnvironmentName();
    
    abstract public String getDefaultPath();
    
    @Override
    public String getPath() {
        // look in system properties first, then environment variable, then parent
        String property = System.getProperty(getPropertyName());
        if( property != null && !property.isEmpty() ) {
            return property;
        }
        String env = System.getenv(getEnvironmentName());
        if( env != null && !env.isEmpty()) {
            return env;
        }
        return getDefaultPath();
    }
    
}
