/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

/**
 * This interface exists for a least-effort observation pattern,
 * where the observer is only notified that its object of interest
 * has changed and the observer is responsible for querying the object
 * to get its current status.
 * 
 * This interface may be used by passive observers together with
 * the Monitor class to get periodic updates about the state of some
 * object. That combination is ideal for progress monitoring since
 * it needs to be done periodically and not on every change to the
 * object (which could dramatically decrease performance)
 * 
 * For active observation -- when you want immediate notification that
 * the object of interest has changed -- you have to use the 
 * java.util.Observable and java.util.Observer classes.
 * 
 * See also java.util.Observer, which is not generic.
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public interface Observer<T> {
    public void observe(T object);
}
