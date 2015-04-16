/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.collection;

/**
 * Wraps another Iterator instance and presents a read-only view.
 * This does NOT mean the Iterator is unmodifiable - any code with
 * a reference to the underlying collection can modify it and those
 * changes will be made visible via this wrapper.
 *
 * @author jbuhacoff
 */
public class ReadonlyIterator<T> implements java.util.Iterator<T> {
    private java.util.Iterator<T> delegate;
    
    /**
     * Wraps (does NOT copy) the original iterator
     * @param original 
     */
    public ReadonlyIterator(java.util.Iterator<T> original) {
        this.delegate = original;
    }
    
    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public T next() {
        return delegate.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
}
