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
public class FilenameStartsWithFilter implements FilenameFilter, Filter<File> {
    private String startsWith;
    
    public FilenameStartsWithFilter() {
        startsWith = null;
    }
    public FilenameStartsWithFilter(String startsWith) {
        this.startsWith = startsWith;
    }
    /**
     * 
     * There is no default value; if the value is not set then no files
     * will be accepted at all.
     * 
     * @param startsWith 
     */
    public void setStartsWith(String startsWith) {
        this.startsWith = startsWith;
    }

    public String getStartsWith() {
        return startsWith;
    }
    
    
    
    @Override
    public boolean accept(File dir, String name) {
        if( startsWith == null || name == null ) { return false; }
        return name.startsWith(startsWith);
    }

    @Override
    public boolean accept(File item) {
        return item.getName().startsWith(startsWith);
    }
    
}
