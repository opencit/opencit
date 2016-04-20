/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
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
