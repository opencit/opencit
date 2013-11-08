/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util;

import com.intel.dcsg.cpg.util.ArrayStack;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class ArrayStackTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ArrayStackTest.class);

    @Test(expected=IndexOutOfBoundsException.class)
    public void testPopEmpty() {
        ArrayStack<String> stack = new ArrayStack<String>();
        stack.pop();
    }
    
    @Test
    public void testPopOrder() {
        ArrayStack<String> stack = new ArrayStack<String>();
        stack.push("a");
        stack.push("b");
        stack.push("c");
        assertEquals("c", stack.pop());
        assertEquals("b", stack.pop());
        assertEquals("a", stack.pop());
        assertTrue(stack.isEmpty());
    }
}
