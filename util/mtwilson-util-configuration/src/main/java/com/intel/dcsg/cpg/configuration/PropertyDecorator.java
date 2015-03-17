/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.Set;

/**
 * This decorator adds {@code get(Property property)} and {@code set(Property property, String value)}
 * to enable programmers to use constants like this:
 * 
 * <pre>
 * public class Example implements Runnable, Configurable {
 * public static final Property MAX_THREADS = new Property("com.example.max.threads", "15");
 * private Configuration conf;
 * public void configure(Configuration conf) {
 * this.conf = conf;
 * }
 * public void run() {
 * System.out.println("Max threads: "+conf.get(MAX_THREADS));
 * }
 * }
 * </pre>
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
    public String get(String key) {
        return configuration.get(key);
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
