/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.mtwilson.setup.AbstractSetupTask;
import java.io.File;

/**
 * 
 * 
 * @author jbuhacoff
 */
public class Filesystem extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Filesystem.class);
    
    private com.intel.mtwilson.Filesystem fs = new com.intel.mtwilson.Filesystem();
    private File configuration;
    private File repository;
    
    @Override
    protected void configure() throws Exception {
        log.debug("Configuration path: {}", fs.getConfigurationPath());
        log.debug("Repository path: {}", fs.getRepositoryPath());
        configuration = new File(fs.getConfigurationPath());
        repository = new File(fs.getRepositoryPath());
    }

    @Override
    protected void validate() throws Exception {
        if( !configuration.isDirectory() ) {
            validation("Configuration folder is not created");
        }
        if( !repository.isDirectory() ) {
            validation("Repository folder is not created");
        }
    }

    @Override
    protected void execute() throws Exception {
        configuration.mkdirs();
        repository.mkdirs();
    }
    
    
   
}
