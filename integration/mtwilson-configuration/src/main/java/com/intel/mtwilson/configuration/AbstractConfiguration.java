/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;

/**
 * THIS CLASS IS TENTATIVE - NOT CURENTLY BEING USED OUTSIDE THIS PACKAGE
 * @author jbuhacoff
 */
public class AbstractConfiguration implements Configurable {
    private Configuration configuration;
    
    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
    
    protected Configuration getConfiguration() {
        return configuration;
    }

}
