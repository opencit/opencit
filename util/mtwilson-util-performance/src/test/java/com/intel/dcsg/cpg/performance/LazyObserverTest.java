/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class LazyObserverTest {
    private static Logger log = LoggerFactory.getLogger(LazyObserverTest.class);
    
    @Test
    public void testStringHashCodeEquals() {
        String text1 = "hello world";
        String text2 = "hello world";
        assertEquals(text1.hashCode(), text2.hashCode());
    }

    @Test
    public void testEmptyArrayListHashCodeEquals() {
        ArrayList<String> list1 = new ArrayList<String>();
        ArrayList<String> list2 = new ArrayList<String>();
        assertEquals(list1.hashCode(), list2.hashCode());
    }
    
    @Test
    public void testIdenticalArrayListHashCodeEquals() {
        ArrayList<String> list1 = new ArrayList<String>();
        list1.add("hello world");
        ArrayList<String> list2 = new ArrayList<String>();
        list2.add("hello world");
        assertEquals(list1.hashCode(), list2.hashCode());
    }
    
}
