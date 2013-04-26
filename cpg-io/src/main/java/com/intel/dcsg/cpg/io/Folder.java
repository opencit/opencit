/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import java.io.File;
import java.io.IOException;

/**
 * Represents a folder (directory) in the file system. This class exists to make it clear
 * when certain methods expect their java.io.File input to be specifically a 
 * folder and not a regular file.
 * 
 * @author jbuhacoff
 */
public class Folder /*extends File*/ {
    private final File file;
    
    /**
     *
     * @param file representing a folder
     */
    public Folder(File file) {
        this.file = file;
    }
    
    /**
     * 
     * @param relative or absolute path to a folder
     */
    public Folder(String path) {
        this(new File(path));
    }
    
    /**
     * 
     * @return the wrapped File object; guaranteed to be an existing folder
     */
    public File getFile() { return file; }
    
    /**
     * If the folder already exists, this method simply returns a Folder
     * representing the existing folder.  
     * If the folder does not already exists, it will be created.
     * @param file representing a path to a directory that should be created 
     * @return a FileFolder representing an existing folder
     * @throws IllegalArgumentException if the provided File already exists but is not a directory
     * @throws IOException if the folder cannot be created
     */
    public static Folder create(File file) throws IOException {
        if( file.exists() && !file.isDirectory() ) {
            throw new IllegalArgumentException("File already exists but is not a directory: "+file.getAbsolutePath());
        }
        if( file.mkdirs() ) {
            Folder folder = new Folder(file);
            return folder;
        }
        throw new IOException("Cannot create directory: "+file.getAbsolutePath());
    }
}
