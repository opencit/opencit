/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

/**
 * A class should implement Value<T> when it may be making changes to "some value"
 * that may be of interest to an observer. Obtaining the value is expected to be
 * very fast and should not throw any exceptions as the value is expected to already
 * exist.
 * 
 * Classes that implement Value<T> are not factory classes. Factory methods are expected
 * to be expensive and create a new instance every time, whereas classes that implement
 * Value<T> may choose to return the same instance each time (although it could be in a different
 * state).
 * 
 * @param <T> the type of object that will be returned by getValue()
 * @since 0.1
 * @author jbuhacoff
 */
public interface Value<T> {
    /**
     * 
     * @return some value or null
     */
    public T getValue();
}
