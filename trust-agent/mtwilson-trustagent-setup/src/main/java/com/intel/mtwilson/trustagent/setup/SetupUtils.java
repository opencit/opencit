/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author ssbangal
 */
public class SetupUtils {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetupUtils.class);
    
    // given a File, ensures that its parent directory exists, creating it if necessary, and throwing PrivacyCAException 
    // if there is a failure
    public static void mkdir(File file) throws IOException {
        if (!file.getParentFile().isDirectory()) {
            if (!file.getParentFile().mkdirs()) {
                log.warn("Failed to create client installation path!");
                throw new IOException("Failed to create client installation path!");
            }
        }
    }

    public static void writeblob(String absoluteFilePath, byte[] encryptedBytes) throws IOException {
        File file = new File(absoluteFilePath);
        mkdir(file); // ensure the parent directory exists
        try(FileOutputStream out = new FileOutputStream(file)) { // throws FileNotFoundException
            IOUtils.write(encryptedBytes, out); // throws IOException
        }
    }    

    public static byte[] readblob(String absoluteFilePath) throws IOException {
        File file = new File(absoluteFilePath);
        try(FileInputStream in = new FileInputStream(file)) { 
            return IOUtils.toByteArray(in);
        }
    }    
    
    public static void writeString(String absoluteFilePath, String data) throws IOException {
        File file = new File(absoluteFilePath);
        mkdir(file); // ensure the parent directory exists
        try(FileOutputStream out = new FileOutputStream(file)) { // throws FileNotFoundException
            IOUtils.write(data, out); // throws IOException
        }
    }    
    
    
}
