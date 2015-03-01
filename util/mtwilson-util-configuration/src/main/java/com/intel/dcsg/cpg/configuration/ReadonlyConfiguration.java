/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.Set;

/**
 * A decorator for any Configuration instance
 * that will protect it from edits by exposing only the reading methods.
 * 
 * New implementations can implement Configuration and use this
 * decorator to provide a read-only view. 
 * 
 * Example:
 * <pre>
 * Configuration readonly = new ReadonlyConfiguration(new PropertiesConfiguration());
 * </pre>
 * 
 * @author jbuhacoff
 */
public class ReadonlyConfiguration extends AbstractConfiguration {
    private Configuration configuration;

    public ReadonlyConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Set<String> keys() {
        return configuration.keys();
    }

    @Override
    public String get(String key) {
        return configuration.get(key);
    }

    public String get(Property property) {
        return configuration.get(property.getName(), property.getDefaultValue());
    }

    @Override
    public void set(String key, String value) {
        throw new UnsupportedOperationException("Configuration is read-only");
    }

    @Override
    public boolean isEditable() {
        return false;
    }
    
}
