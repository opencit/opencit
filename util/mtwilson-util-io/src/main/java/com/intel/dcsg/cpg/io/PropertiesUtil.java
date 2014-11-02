/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author jbuhacoff
 */
public class PropertiesUtil {
    
    /**
     * 
     * @param file
     * @return the properties if the file exists, or null if the file does not exist
     * @throws IOException if there is an error reading the file
     */
    public static Properties load(File file) throws IOException {
        if( !file.exists() ) { return null; }
        try(FileInputStream in = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }
    
    /**
     * 
     * @param file
     * @return the properties loaded from file; never null
     * @throws FileNotFoundException if the file is not found
     * @throws IOException if there is an error reading the file
     */
    public static Properties loadExisting(File file) throws FileNotFoundException, IOException {
        try(FileInputStream in = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }
    
}
