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
 * A read-only configuration that is comprised of one or more configuration
 * sources which are checked in order for each requested property.
 * 
 * The configuration sources are copied from the constructor arguments
 * so if you change the list or array of sources after creating the
 * CompositeConfiguration, it will not be affected.
 * 
 * If a target is defined it's automatically the first source on the list.
 * 
 * @author jbuhacoff
 */
public class CompositeConfiguration extends AbstractConfiguration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompositeConfiguration.class);
    protected final List<Configuration> sources;
    protected final Configuration target;
    
    protected CompositeConfiguration() {
        super();
        this.target = null;
        this.sources = new ArrayList<>();
    }
    
    public CompositeConfiguration(List<Configuration> sources) {
        this(sources, null);
    }
    public CompositeConfiguration(List<Configuration> sources, Configuration target) {
        super();
        this.sources = new ArrayList<>();
        if( target != null ) {
            this.sources.add(target);
        }
        this.sources.addAll(sources);
        this.target = target;
    }
    public CompositeConfiguration(Configuration... sources) {
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
     * @param defaultValue
     * @return the value of key from the first source to have it, or defaultValue if none of the sources have it
     */
    @Override
    public String get(String key, String defaultValue) {
        for(Configuration source : sources) {
            String value = source.get(key, null);
            if( value != null ) {
                return value;
            }
        }
        return defaultValue;
    }
    
    @Override
    public void set(String key, String value) {
        target.set(key, value);
    }

    public Configuration getTarget() {
        return target;
    }

    @Override
    public boolean isEditable() {
        return target != null && target.isEditable();
    }
    
    
}
