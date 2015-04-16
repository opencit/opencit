/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A layered configuration is comprised of one or more configuration sources.
 * When {@code get} is called, it is delegated to the first layer. If that
 * layer does not have the property defined, the request is delegated to 
 * the second layer, and so on. If the last layer does not have the property
 * defined, then null is returned.
 * 
 * A layered configuration is not editable. It can be used in conjunction with
 * a valve configuration to set a single configuration source that can 
 * accept edits.
 * 
 * The configuration sources are copied from the constructor arguments
 * so if you change the list or array of sources after creating the
 * CompositeConfiguration, it will not be affected.
 * 
 * If a target is defined it's automatically the first source on the list.
 * 
 * @author jbuhacoff
 */
public class LayeredConfiguration extends AbstractConfiguration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayeredConfiguration.class);
    protected final List<Configuration> sources;
    
    protected LayeredConfiguration() {
        super();
        this.sources = new ArrayList<>();
    }
    
    public LayeredConfiguration(List<Configuration> sources) {
        super();
        this.sources = new ArrayList<>();
        this.sources.addAll(sources);
    }
    public LayeredConfiguration(Configuration... sources) {
        this(Arrays.asList(sources));
    }

    /**
     * Edit the list of sources returned from this method to affect the
     * sources checked by {@code get}.
     * @return the mutable list of Configuration sources
     */
    public List<Configuration> getSources() {
        return sources;
    }
    
    public Configuration getSource(String key) {
        for(Configuration source : sources) {
            String value = source.get(key, null);
            if( value != null ) {
                return source;
            }
        }
        return null;
    }

    @Override
    public Set<String> keys() {
        HashSet<String> keys = new HashSet();
        for(Configuration source : sources) {
            keys.addAll(source.keys());
        }
        return keys;
    }
        
    /**
     * @param key
     * @return the value of key from the first source to have it, or null if none of the sources have it
     */
    @Override
    public String get(String key) {
        for(Configuration source : sources) {
            String value = source.get(key);
            if( value != null ) {
                return value;
            }
        }
        return null;
    }
    
    @Override
    public void set(String key, String value) {
        throw new UnsupportedOperationException("A layered configuration is not editable");
    }

    @Override
    public boolean isEditable() {
        return false;
    }
    
    
}
