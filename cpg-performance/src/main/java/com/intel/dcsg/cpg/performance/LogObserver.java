/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class LogObserver<T> implements Observer<T> {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public void observe(T object) {
        log.debug("Observed: {}", object.toString());
    }
    
}
