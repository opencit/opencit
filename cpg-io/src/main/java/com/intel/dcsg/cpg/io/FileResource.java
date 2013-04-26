/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A thin wrapper for Java's File class. If the specified file does not exist,
 * then getInputStream() returns null to indicate there is no data available.
 * 
 * Use this class as a parameter to your methods when you want to indicate that
 * you can create the referenced file if it does not already exist.
 *
 *
 * @author jbuhacoff
 */
public class FileResource implements Resource {

    private final File file;

    public FileResource(File file) {
        this.file = file;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        if( file.exists() ) {
            return new FileInputStream(file);                        
        }
        else {
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(file);
    }
}
