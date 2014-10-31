/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag;

import com.intel.mtwilson.pipe.Filter;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Looks for files that have a corresponding ".sig" file next to them.
 * 
 * @author jbuhacoff
 */
public class PlaintextFilenameFilter implements FilenameFilter, Filter<File> {
    private String endsWith;
    
    public PlaintextFilenameFilter() {
        endsWith = ".sig";
    }
    public PlaintextFilenameFilter(String endsWith) {
        this.endsWith = endsWith;
    }

    public void setEndsWith(String endsWith) {
        this.endsWith = endsWith;
    }

    public String getEndsWith() {
        return endsWith;
    }
    
    
    
    @Override
    public boolean accept(File dir, String name) {
        if( endsWith == null || name == null ) { return false; }
        return !name.endsWith(endsWith) && dir.toPath().resolve(name+endsWith).toFile().exists();
    }

    @Override
    public boolean accept(File item) {
        return !item.getName().endsWith(endsWith) && item.getParentFile().toPath().resolve(item.getName()+endsWith).toFile().exists();
    }
    
}
