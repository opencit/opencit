/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.Set;

/**
 * Reads from one configuration source and writes to a different
 * configuration source.
 * 
 * Both reading and writing configurations must be provided in the 
 * constructor. 
 * 
 * @author jbuhacoff
 */
public class ValveConfiguration extends AbstractConfiguration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValveConfiguration.class);
    private Configuration readFrom;
    private Configuration writeTo;

    public ValveConfiguration() {
    }

    
    public ValveConfiguration(Configuration readFrom, Configuration writeTo) {
        this.readFrom = readFrom;
        this.writeTo = writeTo;
    }

    public Configuration getReadFrom() {
        return readFrom;
    }

    public Configuration getWriteTo() {
        return writeTo;
    }

    public void setReadFrom(Configuration readFrom) {
        this.readFrom = readFrom;
    }

    public void setWriteTo(Configuration writeTo) {
        this.writeTo = writeTo;
    }
    
    

    /**
     * Delegates to the reading configuration.
     * @return 
     */
    @Override
    public Set<String> keys() {
        return readFrom.keys();
    }

    /**
     * Delegates to the reading configuration.
     * @param key
     * @return 
     */
    @Override
    public String get(String key) {
        return readFrom.get(key);
    }

    /**
     * Delegates to the writing configuration
     * @param key
     * @param value 
     */
    @Override
    public void set(String key, String value) {
        writeTo.set(key, value);
    }

    /**
     * Delegates to the writing configuration.
     * @return 
     */
    @Override
    public boolean isEditable() {
        return writeTo.isEditable();
    }
    
    
}
