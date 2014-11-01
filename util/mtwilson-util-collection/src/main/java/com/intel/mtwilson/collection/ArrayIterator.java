/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.collection;

import java.util.Iterator;

/**
 * Unlike ArrayIterator in commons-collections, this one uses generics for compile-time type checking.
 * @author jbuhacoff
 */
public class ArrayIterator<T> implements Iterator<T> {
    private T[] array;
    private int index = 0;
    public ArrayIterator(T[] array) {
        this.array = array;
    }
    
    @Override
    public boolean hasNext() {
        if( array == null ) { return false; }
        return index < array.length;
    }

    @Override
    public T next() {
        T next = array[index];
        index++;
        return next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(); // "Cannot remove element from array"); //To change body of generated methods, choose Tools | Templates.
    }
    
}
