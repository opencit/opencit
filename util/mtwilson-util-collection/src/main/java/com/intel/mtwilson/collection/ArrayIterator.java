/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.collection;

import java.util.Iterator;

/**
 * Wraps any array with an Iterator interface. This class is similar to
 * org.apache.commons.collections.iterators.ArrayIterator but that one
 * is not generic so it always returns Object and you have to cast your
 * elements. This class helps prevent typing mistakes by using Java generics.
 * 
 * The remove() operation is not supported by this class and will throw
 * UnsupportedOperationException if you try to use it.
 * @since 0.1
 * @author jbuhacoff
 */
public class ArrayIterator<T> implements Iterator<T> {
    private T[] array = null;
    private int index = 0;
    public ArrayIterator(T[] array) {
        this.array = array;
    }
    @Override
    public boolean hasNext() {
        return array != null && index < array.length;
    }
    @Override
    public T next() {
        index++;
        return array[index-1];
    }
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
