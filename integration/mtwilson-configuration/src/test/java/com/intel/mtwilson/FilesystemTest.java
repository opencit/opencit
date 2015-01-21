/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.mtwilson.Filesystem;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class FilesystemTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    
    @Test
    public void testFilesystem() {
        Filesystem fs = new Filesystem();
        log.debug("Configuration folder: {}", fs.getConfigurationPath());
        log.debug("Configuration file: {}", fs.getConfigurationFile());
        log.debug("Repository folder: {}", fs.getRepositoryPath());
        
    }
}
