/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io.file;

import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * A simple utility class for working with a single directory that contains files
 * @author jbuhacoff
 */
public class FileRepository {
    private final String path;

    /**
     * 
     * @param path for example System.getProperty("user.home") + File.separator + ".app"
     */
    public FileRepository(String path) {
        this.path = path;
    }
    
    /**
     * Creates the folder if it doesn't already exist, including any necessary parent folders
     */
    public void open() {
        File folder = new File(path);
        if( !folder.exists() ) {
            folder.mkdirs();
        }        
    }
    
    /**
     * Currently does not do anything
     */
    public void close() {
        
    }
    
    public void add(String filename, byte[] content) throws IOException {
        try(InputStream in = new ByteArrayInputStream(content)) {
            add(filename, in);
        }
    }

    public void add(String filename, String content) throws IOException {
        try(InputStream in = new ByteArrayInputStream(content.getBytes())) {
            add(filename, in);
        }
    }

    public void add(String filename, Resource content) throws IOException {
        try(InputStream in = content.getInputStream()) {
            add(filename, in);
        }
    }
        
    /**
     * 
     * @param filename
     * @param content input stream to read; is NOT closed by this method
     * @throws IOException 
     */
    public void add(String filename, InputStream content) throws IOException {
        File file = getFile(filename);
        try(FileOutputStream out = new FileOutputStream(file)) { // throws FileNotFoundException
        IOUtils.copy(content, out); // throws IOException
//        IOUtils.closeQuietly(out);                
        }
    }
    
    public void remove(String filename) throws IOException {
        File file = getFile(filename);
        file.delete();
        if( file.exists() ) { 
            throw new IOException("Failed to delete file");
        }
    }
    
    public List<String> list() throws IOException {
        File folder = new File(path);
        if( !folder.exists() ) {
            throw new IOException("Cannot list files: path not exist");
        }
        String[] files = folder.list();
        return Arrays.asList(files);        
    }
    
    public File getFile(String filename) {
        File file = new File(path + File.separator + filename);
        return file;
    }

    public InputStream getInputStream(String filename) throws IOException {
        return getResource(filename).getInputStream(); // throws IOException
    }

    
    public String getString(String filename) throws IOException {
        byte[] content = getBytes(filename);
        return new String(content);
    }
    
    public byte[] getBytes(String filename) throws IOException {
        File file = getFile(filename);
        try(FileInputStream in = new FileInputStream(file)) { // throws FileNotFoundException
        byte[] content = IOUtils.toByteArray(in);
//        IOUtils.closeQuietly(in);
        return content;
        }
    }
    
    public FileResource getResource(String filename) {
        File file = getFile(filename);
        return new FileResource(file);
    }
}
