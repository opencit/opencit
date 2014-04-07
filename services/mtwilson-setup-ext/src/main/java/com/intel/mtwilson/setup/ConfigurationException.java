/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

/**
 * Thrown when a setup task cannot configure itself due to some external 
 * problem such as unable to read from disk or network error.
 * 
 * @author jbuhacoff
 */
public class ConfigurationException extends SetupException {
    public ConfigurationException() {
        super();
    }
    public ConfigurationException(Throwable e) {
        super(e);
    }
    public ConfigurationException(String message) {
        super(message);
    }
    public ConfigurationException(String message, Throwable e) {
        super(message, e);
    }
    
}
