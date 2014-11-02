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
public class FilenameContainsFilter implements FilenameFilter, Filter<File> {
    private String contains;
    
    public FilenameContainsFilter() {
        contains = null;
    }
    public FilenameContainsFilter(String contains) {
        this.contains = contains;
    }

    /**
     * 
     * There is no default value; if the value is not set then no files
     * will be accepted at all.
     * 
     * @param contains 
     */
    public void setContains(String contains) {
        this.contains = contains;
    }

    public String getContains() {
        return contains;
    }
    
    
    
    @Override
    public boolean accept(File dir, String name) {
        if( contains == null || name == null ) { return false; }
        return name.contains(contains);
    }

    @Override
    public boolean accept(File item) {
        return item.getName().contains(contains);
    }
    
}
