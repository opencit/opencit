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
public class AbstractMutableConfiguration implements MutableConfigurable {
    private MutableConfiguration configuration;
    
    @Override
    public void setMutableConfiguration(MutableConfiguration configuration) {
        this.configuration = configuration;
    }
    
    protected MutableConfiguration getMutableConfiguration() {
        return configuration;
    }
    
}
