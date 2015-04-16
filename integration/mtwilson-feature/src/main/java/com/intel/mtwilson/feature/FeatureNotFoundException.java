/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.feature;

/**
 *
 * @author jbuhacoff
 */
public class FeatureNotFoundException extends UnsupportedOperationException {
    /**
     * 
     * @param message the id of the feature
     */
    public FeatureNotFoundException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message the id of the feature
     * @param cause 
     */
    public FeatureNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
