/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util;

/**
 *
 * @author jbuhacoff
 */
public interface MutableTree<T> extends Tree<T> {
    void remove(T node);
    // maybe   insert(T parent, T child); to insert a new child node into the parent node ... but what about position?  insertAt(parent, index, child) ?  with insert(parent,child) assuming index 0 so insert at the front? 
}
