/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class HyphenatedCommandFinderTest {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void testConvertHelloWorld() {
        HyphenatedCommandFinder h = new HyphenatedCommandFinder("test");
        assertEquals("HelloWorld", h.toCamelCase("hello-world"));
        assertEquals("Oneword", h.toCamelCase("oneword"));
        assertEquals("Mtwilson", h.toCamelCase("mtwilson"));
        assertEquals("MtWilson", h.toCamelCase("mtWilson"));
    }

    @Test
    public void testConvertMtWilsonWithMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("mtwilson", "MtWilson");
        HyphenatedCommandFinder h = new HyphenatedCommandFinder("test", map);
        assertEquals("HelloWorld", h.toCamelCase("hello-world"));
        assertEquals("Oneword", h.toCamelCase("oneword"));
        assertEquals("MtWilson", h.toCamelCase("mtwilson"));
        assertEquals("MtWilson", h.toCamelCase("mtWilson"));
    }

}
