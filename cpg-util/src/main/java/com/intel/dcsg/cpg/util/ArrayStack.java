/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util;

import java.util.ArrayList;

/**
 *
 * @author jbuhacoff
 */
public class ArrayStack<E> extends ArrayList<E> implements Stack<E> {

    public void push(E item) {
        add(item);
    }

    public E pop() {
        int size = size();
        E item = get(size-1); // last element, throws IndexOutOfBoundsException if stack is empty
        remove(size-1);
        return item;
    }
    
}
