/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.setup.SetupTask;
import java.util.ArrayList;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class SetupTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetupTest.class);

    @Test
    public void testSetupTasks() {
        ArrayList<SetupTask> tasks = new ArrayList<SetupTask>();
        /*
        tasks.add(new ConfigureFilesystem());
        tasks.add(new CreateMtWilsonPropertiesFile());
        tasks.add(new CreateCertificateAuthorityKey());
        tasks.add(new ConfigureDatabase());
        */
        tasks.add(new InitDatabase());
        
        for(SetupTask task : tasks) {
            if( task.isConfigured() && task.isValidated() ) {
                log.debug("nothing to do for {}", task.getClass().getName());
            }
            else if( !task.isConfigured() ) {
                for(Fault fault : task.getConfigurationFaults()) {
                    log.debug("configuration: {}", fault.toString());
                }
            }
            else if( task.isConfigured() && !task.isValidated() ) {
                task.run();
                log.debug("after run: setup task {} configured? {} validated? {}", task.getClass().getName(), task.isConfigured(), task.isValidated());
                for(Fault fault : task.getConfigurationFaults()) {
                    log.debug("configuration: {}", fault.toString());
                }
                for(Fault fault : task.getValidationFaults()) {
                    log.debug("validation: {}", fault.toString());
                }
            }
            else {
                log.error("unexpected condition setup task {} configured? {} validated? {}", task.getClass().getName(), task.isConfigured(), task.isValidated());
            }
        }
    }
}
