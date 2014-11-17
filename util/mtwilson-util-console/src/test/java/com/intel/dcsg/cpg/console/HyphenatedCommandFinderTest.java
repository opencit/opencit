/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

import com.intel.mtwilson.text.transform.PascalCaseNamingStrategy;
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
        PascalCaseNamingStrategy h = new PascalCaseNamingStrategy();
        assertEquals("HelloWorld", h.toPascalCase("hello-world"));
        assertEquals("Oneword", h.toPascalCase("oneword"));
        assertEquals("Mtwilson", h.toPascalCase("mtwilson"));
        assertEquals("MtWilson", h.toPascalCase("mtWilson"));
    }

    @Test
    public void testConvertMtWilsonWithMap() {
        HashMap<String,String> map = new HashMap<>();
        map.put("mtwilson", "MtWilson");
        PascalCaseNamingStrategy h = new PascalCaseNamingStrategy(map);
        assertEquals("HelloWorld", h.toPascalCase("hello-world"));
        assertEquals("Oneword", h.toPascalCase("oneword"));
        assertEquals("MtWilson", h.toPascalCase("mtwilson"));
        assertEquals("MtWilson", h.toPascalCase("mtWilson"));
    }

}
