/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.collection;

import com.intel.mtwilson.collection.ReadonlyIterator;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Wrapper to present the immutable com.intel.mtwilson.collection.Collection
 * interface around a java.util.Collection.  The presence of keys or their
 * names are immutable, but values of the keys are not immutable and may 
 * change.
 * @author jbuhacoff
 */
public class ImmutableCollection<T> implements Collection<T> {
    private ArrayList<T> delegate;
    public ImmutableCollection(java.util.Collection<T> toCopy) {
        this.delegate = new ArrayList<>();
        this.delegate.addAll(toCopy);
    }
    
    @Override
    public boolean contains(T object) {
        return delegate.contains(object);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Iterator<T> iterator() {
        return new ReadonlyIterator(delegate.iterator());
    }
    
}
