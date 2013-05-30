/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

/**
 * Use this interface whenever you want to make it easy for a stream
 * processor to inform you of its progress. Using the Counter interface
 * it just has to call increment() in every iteration. 
 * 
 * @author jbuhacoff
 */
public interface Counter {
    public void increment();
}
