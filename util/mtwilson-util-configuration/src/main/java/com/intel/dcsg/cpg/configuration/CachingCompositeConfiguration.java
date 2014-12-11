/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.List;

/**
 * This class is tentative
 * 
 * A composite configuration that automatically copies non-null values 
 * from the composite sources
 * into the mutable configuration target as they are accessed.
 * 
 * @author jbuhacoff
 */
public class CachingCompositeConfiguration extends CompositeConfiguration {
    public CachingCompositeConfiguration(List<Configuration> sources, Configuration target) {
        super(sources, target);
    }

    @Override
    public String get(String key, String defaultValue) {
        for(Configuration source : sources) {
            String value = source.get(key, null);
            if( value != null ) {
                if( source != target ) {
                    target.set(key, value);
                }
                return value;
            }
        }
        return defaultValue;
    }
    
}
