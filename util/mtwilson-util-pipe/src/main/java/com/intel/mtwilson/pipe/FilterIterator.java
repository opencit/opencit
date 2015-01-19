/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.pipe;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author jbuhacoff
 */
public class FilterIterator<T> implements Iterator<T> {
    private Filter<T> filter;
    private Iterator<T> iterator;
    private T nextItem;
    public FilterIterator(Filter<T> filter, Iterator<T> iterator) {
        this.filter = filter;
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        if( nextItem != null ) {
            return true;
        }
        while( nextItem == null && iterator.hasNext() ) {
            T item = iterator.next(); // temporary while we evaluate
            if( filter.accept(item)) {
                nextItem = item;
            }
        }
        return nextItem != null;
    }

    @Override
    public T next() {
        if( nextItem == null && !hasNext() ) {
            throw new NoSuchElementException();
        }
        assert nextItem != null;
        // calling next uses up the next item, so we need to set it to null
        // while returning it... therefore we temporarily assign it to a
        // local variable
        T returnItem = nextItem;
        nextItem = null;
        return returnItem;
    }

    /**
     * Delegates to {@code remove()} in the underlying iterator.
     */
    @Override
    public void remove() {
        iterator.remove();
    }
}
