/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

/**
 *
 * @author jbuhacoff
 */
public class EnvironmentConfiguration extends StringConfiguration {

    public EnvironmentConfiguration() {
        super();
    }

    @Override
    protected String get(String key) {
        return System.getenv(key);
    }

}
