/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.mtwilson.Folders;
import com.intel.mtwilson.setup.AbstractSetupTask;
import java.io.File;

/**
 * 
 * 
 * @author jbuhacoff
 */
public class Filesystem extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Filesystem.class);
    
    private File configuration;
    private File repository;
    
    @Override
    protected void configure() throws Exception {
        configuration = new File(Folders.configuration());
        repository = new File(Folders.repository());
        log.debug("Configuration path: {}", configuration.getAbsolutePath());
        log.debug("Repository path: {}", repository.getAbsolutePath());
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
