/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A Configuration implementation backed by any Commons Configuration
 * instance. This is analogous to PropertiesConfiguration backed by a 
 * Properties instance, and MapConfiguration backed by a Map instance.
 * 
 * For the reverse, which is a Commons Configuration interface wrapping
 * our own Configuration instance, use CommonsConfigurationAdapter.
 * 
 * @author jbuhacoff
 */
public class CommonsConfiguration extends AbstractConfiguration {
    private org.apache.commons.configuration.Configuration cc;
    
    public CommonsConfiguration(org.apache.commons.configuration.Configuration cc) {
        this.cc = cc;
    }
    
    @Override
    public Set<String> keys() {
        HashSet<String> keys = new HashSet<>();
        Iterator<String> it = cc.getKeys();
        while(it.hasNext()) {
            keys.add(it.next());
        }
        return keys;
    }

    @Override
    public String get(String key) {
        return cc.getString(key);
    }

    @Override
    public void set(String key, String value) {
        cc.setProperty(key, value);
    }

    @Override
    public boolean isEditable() {
        return true;
    }
    
    
    
}
