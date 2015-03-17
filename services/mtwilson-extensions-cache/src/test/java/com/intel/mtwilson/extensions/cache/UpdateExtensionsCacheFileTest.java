/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.extensions.cache;

import com.intel.dcsg.cpg.validation.Fault;
import java.io.File;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class UpdateExtensionsCacheFileTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UpdateExtensionsCacheFileTest.class);

    @Test
    public void testUpdateExtensionsCacheFile() throws Exception {
        File cacheFile = new File("target"+File.separator+"extensions.cache");
        UpdateExtensionsCacheFile task = new UpdateExtensionsCacheFile();
        task.setCacheFile(cacheFile);
        if( task.isConfigured() ) {
            log.debug("configured ok");
            task.execute();
        }
        else {
            printFaults(task.getConfigurationFaults());
        }
    }

    @Test
    public void testUpdateExtensionsCacheFileValidation() throws Exception {
        File cacheFile = new File("target"+File.separator+"extensions.cache");
        UpdateExtensionsCacheFile task = new UpdateExtensionsCacheFile();
        task.setCacheFile(cacheFile);
        if( task.isConfigured() && task.isValidated() ) {
            log.debug("validated, extensions.cache does not need to be updated");
        }
        else {
            printFaults(task.getConfigurationFaults());
            printFaults(task.getValidationFaults());
        }
    }
    
    private void printFaults(List<Fault> faults) {
        if( faults != null ) {
        for(Fault fault : faults) {
            log.debug("fault: {}", fault.toString());
        }
        }
    }
}
