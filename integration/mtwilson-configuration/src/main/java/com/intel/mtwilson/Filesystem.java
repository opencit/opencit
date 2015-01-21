/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.mtwilson.util.filesystem.Folder;
import com.intel.mtwilson.util.filesystem.Home;
import com.intel.mtwilson.util.filesystem.Subfolder;
import java.io.File;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class Filesystem {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Filesystem.class);
    private String configurationPath;
    private String repositoryPath;
    
    public Filesystem() {
        Folder home = new Home();
        Folder configurationFolder = new Subfolder("configuration", home);
        Folder repositoryFolder = new Subfolder("repository", home);
        configurationPath = configurationFolder.getPath();
        repositoryPath = repositoryFolder.getPath();
    }
    

    /**
     * 
     * @param label localized human-readable, for example "Configuration" or "Repository"
     * @param choices list of paths to try 
     * @return the first path from the list of choices that exists or (if none already exist) the first one that could be created
     * @throws IllegalStateException if no existing directory was found and no directory could be created
     */
    protected String mkdir(String label, List<String> choices) {
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
        log.error("Unable to find {} folder from choices: {}",label, choices);
        throw new IllegalStateException("No configuration folder");
    }
    
    /**
     * 
     * @return path to the configuration folder
     */
    public String getConfigurationPath() {
        return configurationPath;
    }
    
    public String getRepositoryPath() {
        return repositoryPath;
    }

    /**
     * 
     * @return File representing the configuration file kms.conf in the folder returned by @{code getConfiguration()}
     */
    public File getConfigurationFile() {
        String path = getConfigurationPath();
        String filename = System.getProperty("mtwilson.configuration.file", "mtwilson.properties");  // kms overrides with "kms.conf" for example
        File file = new File(path + File.separator + filename);
        return file;
    }
    
}
