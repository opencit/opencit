/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.extensions.cache;

import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.validation.Fault;
import java.io.File;
import java.io.FileFilter;
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
    public void testUpdateExtensionsCacheFileWithFilterInstance() throws Exception {
        File cacheFile = new File("target"+File.separator+"extensions.cache");
        UpdateExtensionsCacheFile task = new UpdateExtensionsCacheFile();
        task.setCacheFile(cacheFile);
        task.setFileIncludeFilter(new AppJarsFilter());
        if( task.isConfigured() ) {
            log.debug("configured ok");
            task.execute();
        }
        else {
            printFaults(task.getConfigurationFaults());
        }
    }
    
    @Test
    public void testUpdateExtensionsCacheFileWithFilterClassName() throws Exception {
        File cacheFile = new File("target"+File.separator+"extensions.cache");
        UpdateExtensionsCacheFile task = new UpdateExtensionsCacheFile();
        task.setCacheFile(cacheFile);
        PropertiesConfiguration configuration = new PropertiesConfiguration();
        configuration.set("mtwilson.extensions.fileIncludeFilter.class", "com.intel.mtwilson.extensions.cache.AppJarsFilter");
        task.setConfiguration(configuration);
        if( task.isConfigured() ) {
            log.debug("configured ok");
            task.execute();
        }
        else {
            printFaults(task.getConfigurationFaults());
        }
    }
    
    @Test
    public void testUpdateExtensionsCacheFileWithFilterContains() throws Exception {
        File cacheFile = new File("target"+File.separator+"extensions.cache");
        UpdateExtensionsCacheFile task = new UpdateExtensionsCacheFile();
        task.setCacheFile(cacheFile);
        PropertiesConfiguration configuration = new PropertiesConfiguration();
        configuration.set("mtwilson.extensions.fileIncludeFilter.contains", "mtwilson"); // can be comma-separated list
        task.setConfiguration(configuration);
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
