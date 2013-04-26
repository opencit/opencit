/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;

import com.intel.dcsg.cpg.io.ExistingFileResource;
import com.intel.dcsg.cpg.io.FileResource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author jbuhacoff
 */
public class TestFileResource {
    
    private File file = new File(System.getProperty("user.home", "."), "test-com_intel_mtwilson_io_TestFileResource.txt");
    
    @Test
    public void testCreateNewFile() throws IOException {
        // start with a clean slate
        if( file.exists() ) { 
            boolean deleted = file.delete();
            if( !deleted ) {
                throw new IOException("Cannot delete existing test file; test results will not be valid");
            }
        }        
        // create a new file
        FileResource resource = new FileResource(file);
        OutputStream out = resource.getOutputStream();
        PrintWriter writer = new PrintWriter(out);
        writer.println("Hello, world!");
        writer.close();
        // check that it exists now
        if( !file.exists() ) {
            fail("file was not created");
        }
    }
    
    @Test
    public void testReadExistingFile() throws IOException {
        testCreateNewFile();
        FileResource resource = new FileResource(file);
        InputStream in = resource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String firstLine = reader.readLine();
        assertEquals("Hello, world!", firstLine);
        reader.close();
    }
    
    @Test
    public void testReadNonexistingFile() throws IOException {
        // make sure the file is deleted
        if( file.exists() ) { 
            boolean deleted = file.delete();
            if( !deleted ) {
                throw new IOException("Cannot delete existing test file; test results will not be valid");
            }
        }        
        // try reading         
        FileResource resource = new FileResource(file);
        InputStream in = resource.getInputStream(); // should return null
        assertNull(in);
    }

    @Test(expected = FileNotFoundException.class)
    public void testReadNonexistingExistingFile() throws IOException {
        // make sure the file is deleted
        if( file.exists() ) { 
            boolean deleted = file.delete();
            if( !deleted ) {
                throw new IOException("Cannot delete existing test file; test results will not be valid");
            }
        }        
        // try reading         
        ExistingFileResource resource = new ExistingFileResource(file);
        InputStream in = resource.getInputStream(); // should throw FileNotFoundException
        in.close();
    }

}
