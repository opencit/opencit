/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.CommonsConfigurationAdapter;
import com.intel.dcsg.cpg.configuration.Configuration;

/**
 * To use an Apache Commons Configuration instance:
 * 
 * <pre>
 * instance.setConfiguration(new CommonsConfigurationAdapter(configuration));
 * </pre>
 * 
 * @author jbuhacoff
 */
public class AbstractConfiguration implements Configurable {
    private Configuration configuration;
    
    @Override
    public void configure(Configuration configuration) {
        this.configuration = configuration;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Convenience method to allow using an Apache Commons Configuration 
     * instance 
     * @param configuration 
     */
    public void setConfiguration(org.apache.commons.configuration.Configuration configuration) {
        this.configuration = new CommonsConfigurationAdapter(configuration);
    }
}
