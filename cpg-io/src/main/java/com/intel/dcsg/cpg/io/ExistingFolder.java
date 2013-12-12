/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import java.io.File;

/**
 * This class can be used as a parameter by methods that specifically require
 * an existing folder on the file system as input. 
 * 
 * @author jbuhacoff
 */
public class ExistingFolder extends Folder {
    
    /**
     * 
     * @param folderPath representing an existing folder
     */
    public ExistingFolder(String folderPath) {
        this(new File(folderPath));
    }
    
    /**
     *
     * @param folderFile representing an existing folder
     */
    public ExistingFolder(File folderFile) {
        super(folderFile);
        if( !folderFile.exists() ) {
            throw new IllegalArgumentException("Does not exist: "+folderFile.getAbsolutePath());
        }
        if( !folderFile.isDirectory() ) {
            throw new IllegalArgumentException("Not a directory: "+folderFile.getAbsolutePath());
        }
    }
}
