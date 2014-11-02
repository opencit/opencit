/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.http;

import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class QueryTest {
    @Test
    public void testQueryString() {
        MutableQuery query = new MutableQuery();
        query.add("key1", "value1");
        query.add("key2", "value2a");
        query.add("key2", "value2b");
        assertEquals("key1=value1&key2=value2a&key2=value2b", query.toString());
        query.removeAll("key2");
        assertEquals("key1=value1", query.toString());
        query.clear();
        assertEquals("", query.toString());
    }
    
    @Test
    public void testQueryWithEncodedValues() {
        MutableQuery query = new MutableQuery();
        query.add("key1", "value with space");
        query.add("key2", ".-*_");
        query.add("key3", "ü@foo-bar");
        query.add("key four", "");
        assertEquals("key1=value+with+space&key2=.-*_&key3=%C3%BC%40foo-bar&key+four=", query.toString());
    }
    
    @Test
    public void testEmptyQuery() {
        MutableQuery query = new MutableQuery();
        assertEquals("", query.toString());
    }
}
