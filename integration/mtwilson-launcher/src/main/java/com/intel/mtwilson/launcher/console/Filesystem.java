/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher.console;

import com.intel.mtwilson.util.filesystem.Folder;
import com.intel.mtwilson.util.filesystem.Home;
import com.intel.mtwilson.util.filesystem.Subfolder;

/**
 *
 * @author jbuhacoff
 */
public class Filesystem {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Filesystem.class);
    private String configurationPath;
    private String featuresPath;
    private String repositoryPath;
    
    public Filesystem() {
        Folder home = new Home();
        Folder configurationFolder = new Subfolder("configuration", home);
        Folder featuresFolder = new Subfolder("features", home);
        Folder repositoryFolder = new Subfolder("repository", home);
        configurationPath = configurationFolder.getPath();
        featuresPath = featuresFolder.getPath();
        repositoryPath = repositoryFolder.getPath();
    }
    
    /**
     * 
     * @return path to the configuration folder
     */
    public String getConfigurationPath() {
        return configurationPath;
    }

    public String getFeaturesPath() {
        return repositoryPath;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

}
