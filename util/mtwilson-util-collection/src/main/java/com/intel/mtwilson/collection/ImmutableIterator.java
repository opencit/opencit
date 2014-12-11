/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.collection;

import java.util.Iterator;

/**
 *
 * @author jbuhacoff
 */
public class ImmutableIterator<T> implements Iterator<T> {
    private Iterator<T> delegate;

    public ImmutableIterator(Iterator<T> delegate) {
        this.delegate = delegate;
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
        throw new UnsupportedOperationException("Attempted to remove an item from an immutable iterator");
    }
    
}
