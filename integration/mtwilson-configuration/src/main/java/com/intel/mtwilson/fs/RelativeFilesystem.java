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

    @Override
    public String getApplicationRoot() {
        return root;
    }
}
