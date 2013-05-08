/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

/**
 *
 * @author jbuhacoff
 */
public abstract class BaseRule implements Rule {
    protected String[] markers = null;
    
    @Override
    abstract public RuleResult apply(HostReport hostReport);

    @Override
    public String[] getMarkers() {
        return markers;
    }
    
    public void setMarkers(String... markers) {
        this.markers = markers;
    }
}
