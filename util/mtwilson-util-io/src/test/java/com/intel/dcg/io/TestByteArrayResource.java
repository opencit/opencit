/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;

import com.intel.dcsg.cpg.io.ByteArrayResource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class TestByteArrayResource {
    
    @Test
    public void testReadFromNullResource() throws IOException {
        ByteArrayResource resource = new ByteArrayResource(null);
        InputStream in = resource.getInputStream();
        assertNull(in);
        if( in != null ) {
            in.close();
        }
    }
    
    @Test
    public void testReadFromEmptyResource() throws IOException {
        ByteArrayResource resource = new ByteArrayResource(); // can also pass new byte[] { }  or new byte[0] which is the same as what the no-arg constructor does
        InputStream in = resource.getInputStream();
        assertNull(in);
        if( in != null ) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            System.out.println(reader.readLine()); // immediate EOF, would display "null"
            reader.close();
        }
    }

    @Test
    public void testWriteToNullResource() throws IOException {
        ByteArrayResource resource = new ByteArrayResource(null);
        OutputStream out = resource.getOutputStream();
        PrintWriter writer = new PrintWriter(out);
        writer.println("Hello, world!");
        writer.close();
        // now try reading 
        InputStream in = resource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String firstLine = reader.readLine();
        assertEquals("Hello, world!", firstLine);
        reader.close();
    }
}
