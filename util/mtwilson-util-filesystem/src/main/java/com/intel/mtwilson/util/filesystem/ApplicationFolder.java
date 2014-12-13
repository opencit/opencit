/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.filesystem;

/**
 * Represents a folder that can be configured via a system property or
 * environment variable and has a default path if neither of those is set. 
 * 
 * The application name can be set using the "app.name" system property; it
 * cannot be set via an environment variable. 
 * 
 * @author jbuhacoff
 */
public abstract class ApplicationFolder extends ConfigurableFolder {
    private final String app;
    
    public ApplicationFolder() {
        super();
        app = System.getProperty("mtwilson.application.id", "mtwilson");        
    }
    
    protected String getApplicationName() { return app; }
    
}
