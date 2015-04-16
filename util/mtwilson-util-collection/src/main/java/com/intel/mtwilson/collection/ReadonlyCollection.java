/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Wraps another Collection instance and presents a read-only view.
 * This does NOT mean the Collection is unmodifiable - any code with
 * a reference to the underlying collection can modify it and those
 * changes will be made visible via this wrapper.
 * 
 * Also, the readonly mechanism does not protect individual items in
 * the collection. So a readonly view of some "Circle" objects would
 * still permit those instances to be modified, and anyone sharing
 * a reference to them would see the changes. It is only the collection
 * itself that is readonly.
 * 
 * @author jbuhacoff
 */
public class ReadonlyCollection<T> implements java.util.Collection<T> {
    private java.util.Collection<T> delegate;
    /**
     * Wraps (does not copy) original collection.
     * @param original 
     */
    public ReadonlyCollection(java.util.Collection<T> original) {
        this.delegate = original;
    }
    
    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return new ReadonlyIterator<>(delegate.iterator());
    }

    @Override
    public Object[] toArray() {
        Object[] array = delegate.toArray();
        Object[] copy = Arrays.copyOf(array, array.length);
        return copy;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        T[] array = delegate.toArray(a);
        T[] copy = Arrays.copyOf(array, array.length);
        return copy;
    }

    @Override
    public boolean add(T e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
    
}
