/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.extensions.cache;

import java.io.File;
import java.io.FileFilter;

/**
 * A simple file filter that can limit the extensions scan to only jar files
 * having one or more specified keywords in their filenames, for example
 * "mtwilson" or "director".
 * 
 * @author jbuhacoff
 */
public class FileNameContains implements FileFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileNameContains.class);
    private String[] keywords;

    /**
     * Empty constructor must be supported for dynamic instantiation but
     * the {@code setKeywords} must be called before using the filter 
     * in order to set the keywords.
     */
    public FileNameContains() {
        keywords = null;
    }

    public FileNameContains(String[] keywords) {
        this.keywords = keywords;
    }

    
    
    /**
     * Only files including at least one of the specified keywords will be
     * accepted by the filter. If the keywords list is empty then no files
     * will match. It is an error to use a null keywords list.
     * 
     * @param keywords 
     */
    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public String[] getKeywords() {
        return keywords;
    }
    
    
    @Override
    public boolean accept(File pathname) {
        String filename = pathname.getName();
        for(String keyword : keywords) {
            log.debug("Checking filename {} against keyword {}", filename, keyword);
            if( filename.contains(keyword) ) { return true; }
        }
        return false;
    }
    
}
