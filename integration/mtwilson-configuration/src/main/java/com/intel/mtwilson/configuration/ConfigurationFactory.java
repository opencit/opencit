/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurationFactory {
    private static Configuration conf;
    public static Configuration getConfiguration() {
        if( conf == null ) {
            // find the provider (if null) - provider handles encryption & IO
            // ask for the configuration instance from the provider, which will be read-write
            // make an immutable configuration instance from it
        }
        return conf;
    }
}
