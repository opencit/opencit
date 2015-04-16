/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import java.io.IOException;

/**
 * Use the EmptyConfigurationProvider to provide an empty configuration to an
 * object that it can populate and save. 
 * 
 * The {@code load} method returns a new, empty Configuration instance, until
 * the {@code save} method is called. After the {@code save} method is called
 * at least once, subsequent calls to {@code load} will delegate to the
 * underlying ConfigurationProvider.  The {@code save} method always delegates
 * to the underlying ConfigurationProvider.
 * 
 * @author jbuhacoff
 */
public class EmptyConfigurationProvider implements ConfigurationProvider {
    private ConfigurationProvider delegate;
    private boolean created = false;

    public EmptyConfigurationProvider(ConfigurationProvider delegate) {
        this.delegate = delegate;
    }
    
    
    @Override
    public Configuration load() throws IOException {
        if( created ) {
            return delegate.load();
        }
        return new PropertiesConfiguration();
    }

    @Override
    public void save(Configuration configuration) throws IOException {
        delegate.save(configuration);
        created = true;
    }
    
}
