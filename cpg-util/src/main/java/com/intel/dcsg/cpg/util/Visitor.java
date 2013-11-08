/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util;

/**
 *
 * @author jbuhacoff
 */
public interface Visitor<T> {
    void visit(T item);
}
