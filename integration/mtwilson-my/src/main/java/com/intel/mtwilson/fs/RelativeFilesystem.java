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
public class RelativeFilesystem extends AbstractFilesystem {

    private String root;
    public RelativeFilesystem() { root = "."; }
    public RelativeFilesystem(String root ) {
        this.root = root;
    }
    public void setRootPath(String root) {
        this.root = root;
    }
    public String getRootPath() { return root; }
    
    @Override
    protected String getDefaultConfigurationPath() {
        return root + File.separator + "configuration";
    }

    @Override
    protected String getDefaultApplicationPath() {
        return root;
    }
}
