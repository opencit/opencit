/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.as.common;

import com.intel.mtwilson.util.TextUtil;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class TextUtilTest {
    
    public TextUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of join method, of class TextUtil.
     */
    @Test
    public void testJoin() {
        System.out.println("join");
        Collection<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        String expResult = "a, b";
        String result = TextUtil.join(list);
        assertEquals(expResult, result);
    }
}
