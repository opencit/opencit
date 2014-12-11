/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.Set;

/**
 *
 * @author jbuhacoff
 */
public class PropertyDecorator extends AbstractConfiguration {
    private Configuration configuration;

    public PropertyDecorator(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Set<String> keys() {
        return configuration.keys();
    }

    @Override
    public String get(String key, String defaultValue) {
        return configuration.get(key, defaultValue);
    }

    @Override
    public void set(String key, String value) {
        configuration.set(key, value);
    }
    
    public String get(Property property) {
        return configuration.get(property.getName(), property.getDefaultValue());
    }
    
    public void set(Property property, String value) {
        configuration.set(property.getName(), value);
    }

    @Override
    public boolean isEditable() {
        return configuration.isEditable();
    }
    
    
}
