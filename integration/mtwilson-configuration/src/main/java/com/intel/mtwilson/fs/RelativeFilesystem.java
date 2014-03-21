/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

import java.io.File;

/**
 *
 * @author jbuhacoff
 */
public class RelativeFilesystem implements PlatformFilesystem {

    private String root;

    public RelativeFilesystem() {
        root = ".";
    }

    public RelativeFilesystem(String root) {
        this.root = root;
    }

    /**
     * Return the location where applications should be installed.
     * Even though the basic implementation of this is simply the
     * current directory ".", 
     * another implementation could return "./target" or any other
     * relative (or absolute) location.
     * @return 
     */
    @Override
    public String getApplicationRoot() {
        return root;
    }
}
