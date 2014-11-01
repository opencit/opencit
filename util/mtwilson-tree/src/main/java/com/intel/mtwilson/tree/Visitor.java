/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tree;

/**
 *
 * @author jbuhacoff
 */
public interface Visitor<T> {
    void visit(T item);
}
