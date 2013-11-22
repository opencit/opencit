/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class VersionTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VersionTest.class);
    
    @Test
    public void testConstructor() {
        assertEquals("0.1", new Version(0,1).toString());
        assertEquals("1.2.3", new Version(1,2,3).toString());
        assertEquals("2.4-SNAPSHOT", new Version(2,4,"SNAPSHOT").toString());
        assertEquals("2.5.6-RELEASE", new Version(2,5,6,"RELEASE").toString());
    }

    @Test
    public void testParser() {
        assertEquals(new Version(0,1).toString(), Version.valueOf("0.1").toString());
        assertEquals(new Version(1,2,3).toString(), Version.valueOf("1.2.3").toString());
        assertEquals(new Version(2,4,"SNAPSHOT").toString(), Version.valueOf("2.4-SNAPSHOT").toString());
        assertEquals(new Version(2,5,6,"RELEASE").toString(), Version.valueOf("2.5.6-RELEASE").toString());
    }
    
    @Test
    public void testRangeConstructor() {
        assertEquals("[0.1,1.0)", new VersionRange(new Version(0,1), new Version(1,0)).toString());
        assertEquals("[1.2.3,1.2.3]", new VersionRange(new Version(1,2,3), VersionRange.End.CLOSED,new Version(1,2,3),VersionRange.End.CLOSED).toString());
    }

    @Test
    public void testRangeParser() {
        assertEquals(new VersionRange(new Version(0,1), new Version(1,0)).toString(), VersionRange.valueOf("[0.1,1.0)").toString());
        assertEquals(new VersionRange(new Version(1,2,3), VersionRange.End.CLOSED,new Version(1,2,3),VersionRange.End.CLOSED).toString(), VersionRange.valueOf("[1.2.3,1.2.3]").toString());
    }
    
    @Test
    public void testComparison() {
        
    }
}
