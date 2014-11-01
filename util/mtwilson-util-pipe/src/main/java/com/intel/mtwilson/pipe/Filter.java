/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.pipe;

/**
 *
 * @author jbuhacoff
 */
public interface Filter<T> {
    boolean accept(T item);
}
