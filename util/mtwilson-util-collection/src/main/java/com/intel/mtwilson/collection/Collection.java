/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.collection;

/**
 *
 * @author jbuhacoff
 */
public interface Collection<T> extends Iterable<T> {
    boolean contains(T object);
    boolean isEmpty();
    int size();
}
