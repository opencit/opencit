/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io.file;

import com.intel.dcsg.cpg.util.Filter;
import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author jbuhacoff
 */
public class DirectoryFilter implements FilenameFilter, Filter<File> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirectoryFilter.class);

    public boolean skipDotDirs = true;

    /**
     * Implements FilenameFilter, wherein the file to be filtered is the name argument and it's inside the dir argument.
     *
     * @param dir
     * @param name
     * @return
     */
    @Override
    public boolean accept(File dir, String name) {
        try {
            if (skipDotDirs && name.startsWith(".")) {
                return false;
            } // exclude ~/.m2/repository/.cache
            return dir.toPath().resolve(name).toFile().isDirectory();
        } catch (Exception e) {
            log.error("Cannot evaluate file: {}", name, e);
            return false;
        }
    }

    /**
     * Implements the Filter<File> interface for a tree search. In this interface the item to be filtered is a file or a
     * directory and we are only interested in traversing directories (but we process jars)
     *
     * @param item
     * @return
     */
    @Override
    public boolean accept(File item) {
        try {
            if (skipDotDirs && item.getName().startsWith(".")) {
                return false;
            }

            return item.isDirectory();
        } catch (Exception e) {
            log.error("Cannot evaluate file: {}", item, e);
            return false;
        }
    }
}