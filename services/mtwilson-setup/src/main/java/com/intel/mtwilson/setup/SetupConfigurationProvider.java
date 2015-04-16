/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.dcsg.cpg.configuration.CommonsValveConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.EnvironmentConfiguration;
import com.intel.dcsg.cpg.configuration.KeyTransformerConfiguration;
import com.intel.dcsg.cpg.configuration.LayeredConfiguration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.configuration.ValveConfiguration;
import com.intel.mtwilson.text.transform.AllCapsNamingStrategy;
import java.io.IOException;
//import org.apache.commons.configuration.CompositeConfiguration;
//import org.apache.commons.configuration.Configuration;
//import org.apache.commons.configuration.ConfigurationException;
//import org.apache.commons.configuration.EnvironmentConfiguration;
//import org.apache.commons.configuration.MapConfiguration;

/**
 * This ConfigurationProvider should be used only by setup tasks because it
 * allows java system properties and environment variables to be used 
 * as defaults when information is not present in the configuration file.
 * When saving the configuration, only changes to the configuration file
 * are saved. 
 * 
 * @author jbuhacoff
 */
public class SetupConfigurationProvider implements ConfigurationProvider {
    private ConfigurationProvider delegate;
//    private Configuration configuration;
    public SetupConfigurationProvider(ConfigurationProvider provider) {
        delegate = provider;
    }
    
    @Override
    public Configuration load() throws IOException {
        // java system properties
        PropertiesConfiguration systemProperties = new PropertiesConfiguration(System.getProperties());
        // environment variables, automatically translating from key.name to KEY_NAME,
        EnvironmentConfiguration environment = new EnvironmentConfiguration();
        KeyTransformerConfiguration allCapsEnvironment = new KeyTransformerConfiguration(new AllCapsNamingStrategy(), environment);
        // designated configuration source (from the delegate provider we got in the constructor)
        Configuration file = delegate.load();
        // layer the configuration sources in order:
        LayeredConfiguration composite = new LayeredConfiguration(systemProperties, environment, allCapsEnvironment, file);

        // use the valve to ensure that changes only go to the writable configuration (and not to system properties or environment)
        ValveConfiguration valve = new ValveConfiguration(composite, file);
//        valve.setReadFrom(composite);
//        valve.setWriteTo(file);
        return valve;
    }

    @Override
    public void save(Configuration configuration) throws ConfigurationException, IOException {
        if( configuration instanceof ValveConfiguration ) {
            // note that this will save only changes to the delegate's loaded configuration
            // (as expected) and it will not save the env vars or system properties 
            ValveConfiguration valve = (ValveConfiguration)configuration;
            delegate.save(valve.getWriteTo());
        }
        else {
            // apparently not the same instance we provided from load() so just save it
            delegate.save(configuration);
        }
    }
    
}
