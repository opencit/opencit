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
public class TestSubfolder {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestSubfolder.class);

    @Test
    public void testDefaultSubfolderWithParent() {
        Subfolder subfolder = new Subfolder("data", new Home());
        assertEquals("mtwilson", subfolder.getApplicationName()); // default is "mtwilson"
        assertEquals("mtwilson.fs.data", subfolder.getPropertyName());
        assertEquals("MTWILSON_FS_DATA", subfolder.getEnvironmentName());
        if( Platform.isUnix() ) {
            assertEquals("/opt/mtwilson/data", subfolder.getPath());
        }
        if( Platform.isWindows() ) {
            assertEquals("C:\\mtwilson\\data", subfolder.getPath());
        }
    }

    @Test
    public void testDefaultSubfolderWithoutParent() {
        Subfolder subfolder = new Subfolder("data");
        assertEquals("mtwilson", subfolder.getApplicationName()); // default is "mtwilson"
        assertEquals("mtwilson.fs.data", subfolder.getPropertyName());
        assertEquals("MTWILSON_FS_DATA", subfolder.getEnvironmentName());
        if( Platform.isUnix() ) {
            assertEquals("data", subfolder.getPath());
        }
        if( Platform.isWindows() ) {
            assertEquals("data", subfolder.getPath());
        }
    }
    
    @Test
    public void testRenamedSubfolderWithParent() {
        System.setProperty("app.name", "TestApp");
        Subfolder subfolder = new Subfolder("data", new Home());
        assertEquals("TestApp", subfolder.getApplicationName()); // we just set it
        assertEquals("testapp.fs.data", subfolder.getPropertyName());
        assertEquals("TESTAPP_FS_DATA", subfolder.getEnvironmentName());
        if( Platform.isUnix() ) {
            assertEquals("/opt/TestApp/data", subfolder.getPath());
        }
        if( Platform.isWindows() ) {
            assertEquals("C:\\TestApp\\data", subfolder.getPath());
        }
        // restore default in case any other tests use it
        System.clearProperty("app.name");
    }
    
}
