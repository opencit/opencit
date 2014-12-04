/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.filesystem;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class TestHome {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestHome.class);

    @Test
    public void testDefaultHomeFolder() {
        Home home = new Home();
        assertEquals("mtwilson", home.getApplicationName()); // default is "mtwilson"
        assertEquals("mtwilson.home", home.getPropertyName());
        assertEquals("MTWILSON_HOME", home.getEnvironmentName());
        if( Platform.isUnix() ) {
            assertEquals("/opt/mtwilson", home.getPath());
        }
        if( Platform.isWindows() ) {
            assertEquals("C:\\mtwilson", home.getPath());
        }
    }
    
    @Test
    public void testRenamedHomeFolder() {
        System.setProperty("app.name", "TestApp");
        Home home = new Home();
        assertEquals("TestApp", home.getApplicationName()); // we just set it
        assertEquals("testapp.home", home.getPropertyName());
        assertEquals("TESTAPP_HOME", home.getEnvironmentName());
        if( Platform.isUnix() ) {
            assertEquals("/opt/TestApp", home.getPath());
        }
        if( Platform.isWindows() ) {
            assertEquals("C:\\TestApp", home.getPath());
        }
        // restore default in case any other tests use it
        System.clearProperty("app.name");
    }
    
}
