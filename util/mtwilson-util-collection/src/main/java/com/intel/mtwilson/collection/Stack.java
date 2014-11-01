/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.collection;

/**
 * The Java library defines Stack as a concrete class that uses Vector as the storage
 * implementation. The Apache Commons library defines an ArrayStack class that extends
 * ArrayList and also implements a Buffer interface which is just Iterator but
 * defines remove() as "get and remove next element" (Iterator's hasNext/next/remove) 
 * and get() as "get next element and do not remove it" (Iterator's hasNext/next).
 * Neither of them define a Stack interface that can be implemented by different 
 * concrete classes.
 * @author jbuhacoff
 */
public interface Stack<E> {
    void push(E item);
    E pop();
    boolean isEmpty();
    // don't know if we need peek() here or if it should be in a different interface
    // don't know if we need search() here like java.util.Stack that returns a 1-based index 
}
