/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.collection;

import com.intel.mtwilson.collection.ArrayIterator;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ArrayIteratorTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ArrayIteratorTest.class);

    @Test
    public void testArrayIterator() {
        String[] array = new String[] { "foo", "bar", "baz" };
        ArrayIterator<String> it = new ArrayIterator<>(array);
        while(it.hasNext()){
            log.debug("next: {}", it.next());
        }
    }
    
    @Test
    public void testEmptyArrayIterator() {
        String[] array = new String[0];
        ArrayIterator<String> it = new ArrayIterator<>(array);
        while(it.hasNext()){
            log.debug("next: {}", it.next());
        }
    }
    
}
