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
 * then getInputStream() throws a FileNotFoundException.
 * 
 * Use this class as a parameter to your methods when you want to indicate that
 * you expect the referenced file to already exist, and cannot create it if it
 * is missing.
 *
 * @author jbuhacoff
 */
public class ExistingFileResource implements Resource {

    private final File file;

    public ExistingFileResource(File file) {
        this.file = file;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(file);  // will throw FileNotFoundException if the file does not exist
    }

    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(file);
    }

}
