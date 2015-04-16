/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.collection;

import java.util.Collection;
import java.util.Iterator;

/**
 * Allows sequential combination of multiple iterators to appear as a single
 * iterator. All iterators must be of the same type.
 * 
 * @author jbuhacoff
 */
public class ChainedIterator<T> implements Iterator<T> {
    private Iterator<T>[] array;
    private int index = 0;

    public ChainedIterator(Iterator<T>... array) {
        this.array = array;
    }
    
    public ChainedIterator(Collection<Iterator<T>> collection) {
        this((Iterator<T>[])collection.toArray());
    }
    
    /**
     * Delegates {@code hasNext()} to the current iterator but if that one 
     * returns false, automatically proceeds to the next iterator
     * in the chain. 
     * @return true if the current or any subsequent iterator in the chain @{code hasNext()} returns true, false if no elements remain in any iterator
     */
    @Override
    public boolean hasNext() {
        if( array == null ) { return false; }
        while( index < array.length ) {
            if( array[index].hasNext() ) {
                return true;
            }
            index++;
        }
        return false;
    }

    /**
     * Delegates {@code next()} to the current iterator. You must call
     * {@code hasNext()} before calling this method or else it will not
     * proceed to the next chained iterator.
     * @return 
     */
    @Override
    public T next() {
        return array[index].next();
    }

    /**
     * Delegates {@code remove()} to the current iterator.
     */
    @Override
    public void remove() {
        array[index].remove();
    }
    
}
