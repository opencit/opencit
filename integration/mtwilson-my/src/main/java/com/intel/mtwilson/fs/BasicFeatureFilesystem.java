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
public class BasicFeatureFilesystem implements FeatureFilesystem {

    private String root = null;
    
    public BasicFeatureFilesystem() {
        
    }
    
    public BasicFeatureFilesystem(String root) {
        this.root = root;
    }
    
    public void setRootPath(String root) {
        this.root = root;
    }
    public String getRootPath() { return root; }

    
    @Override
    public String getBinPath() {
        return root + File.separator + "bin";
    }
    @Override
    public String getHypertextPath() {
        return root + File.separator + "hypertext";
    }
    @Override
    public String getJavaPath() {
        return root + File.separator + "java";
    }
    @Override
    public String getLicensePath() {
        return root + File.separator + "license.d";
    }
    @Override
    public String getLinuxUtilPath() {
        return root + File.separator + "linux-util";
    }
    @Override
    public String getSqlPath() {
        return root + File.separator + "sql";
    }
    
    @Override
    public String getVarPath() {
        return root + File.separator + "var";
    }

}
