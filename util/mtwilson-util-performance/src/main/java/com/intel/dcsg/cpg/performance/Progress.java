/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

/**
 *
 * @author jbuhacoff
 */
public interface Progress {
    /**
     * Examples of current/max:   1/5, 2/5, ..., 5/5
     * @return a 1-based index indicating the current step out of max possible steps; or zero if the first step hasn't been started yet
     */
    Long getCurrent();
    
    /**
     * 
     * @return the maximum number of steps that can be performed
     */
    Long getMax();
}
