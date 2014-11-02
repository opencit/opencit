/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io.file;

import com.intel.mtwilson.pipe.Filter;
import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author jbuhacoff
 */
public class FilenameEndsWithFilter implements FilenameFilter, Filter<File> {
    private String endsWith;
    
    public FilenameEndsWithFilter() {
        endsWith = null;
    }
    public FilenameEndsWithFilter(String endsWith) {
        this.endsWith = endsWith;
    }

    /**
     * If you are looking for files with an extension like .jar you need
     * to include the dot ".jar" 
     * 
     * There is no default value; if the value is not set then no files
     * will be accepted at all.
     * 
     * @param endsWith 
     */
    public void setEndsWith(String endsWith) {
        this.endsWith = endsWith;
    }

    public String getEndsWith() {
        return endsWith;
    }
    
    
    
    @Override
    public boolean accept(File dir, String name) {
        if( endsWith == null || name == null ) { return false; }
        return name.endsWith(endsWith);
    }

    @Override
    public boolean accept(File item) {
        return item.getName().endsWith(endsWith);
    }
    
}
