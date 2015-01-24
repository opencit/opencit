/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import java.io.File;
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
        log.debug("Configuration folder: {}", Folders.configuration());
        log.debug("Configuration file: {}", (new MyConfiguration()).getConfigurationFile().getAbsolutePath());
        log.debug("Repository folder: {}", Folders.application()+File.separator+"repository");
        
    }
}
