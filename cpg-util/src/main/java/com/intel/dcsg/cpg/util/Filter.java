/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util;

/**
 *
 * @author jbuhacoff
 */
public interface Filter<T> {
    boolean accept(T item);
}
