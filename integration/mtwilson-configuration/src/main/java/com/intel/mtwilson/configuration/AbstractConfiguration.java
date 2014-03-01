/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.MutableConfiguration;

/**
 * THIS CLASS IS TENTATIVE - NOT CURENTLY BEING USED OUTSIDE THIS PACKAGE
 * @author jbuhacoff
 */
public class AbstractConfiguration implements Configurable {
    private MutableConfiguration configuration;
    
    @Override
    public void setConfiguration(MutableConfiguration configuration) {
        this.configuration = configuration;
    }
    
    protected MutableConfiguration getConfiguration() {
        return configuration;
    }
    
}
