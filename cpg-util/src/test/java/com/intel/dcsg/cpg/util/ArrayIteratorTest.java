/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util;

import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class ArrayIteratorTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ArrayIteratorTest.class);
    
    @Test
    public void testArray() {
        ArrayIterator<Integer> it = new ArrayIterator<Integer>(new Integer[] { 1, 2, 3, 4 });
        assertTrue(it.hasNext());
        while(it.hasNext()) {
            log.debug("Next: {}", it.next());
        }
    }
    
    @Test
    public void testEmptyArray() {
        ArrayIterator<Integer> it = new ArrayIterator<Integer>(new Integer[0]);
        assertFalse(it.hasNext());
        while(it.hasNext()) {
            log.debug("Next: {}", it.next());
        }        
    }
    
    @Test(expected=NullPointerException.class)
    public void testNullArrayThrowsException() {
        ArrayIterator<Integer> it = new ArrayIterator<Integer>(null);
        assertFalse(it.hasNext()); // throws NullPointerException
    }
}
