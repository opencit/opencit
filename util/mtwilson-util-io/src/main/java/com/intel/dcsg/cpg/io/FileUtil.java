/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import java.io.File;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class FileUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileUtil.class);

    /**
     * Returns the first directory that exists or can be created from the list of choices provided.
     * 
     * @param choices list of paths to try 
     * @return the first path from the list of choices that exists or (if none already exist) the first one that could be created
     * @throws IllegalStateException if no existing directory was found and no directory could be created
     */
    public static String requireDirectory(List<String> choices) {
        String found = null;
        // look for an existing folder among the choices
        for (String path : choices) {
            File file = new File(path);
            if (file.exists()) {
                found = path;
                break;
            }
        }
        if( found != null ) { return found; }
        // didn't find an existing folder; try to create a folder
        for(String path : choices) {
            File file = new File(path);
            try {
                if( file.mkdirs() ) {
                    found = path;
                    break;
                }
            }
            catch(Throwable e) {
                log.debug("Cannot create folder: {}", path, e);
            }
        }
        if( found != null ) { return found; }
        log.error("Unable to find directory from choices: {}", choices);
        throw new IllegalStateException("No directory");
    }
    
}
