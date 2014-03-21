/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.setup.SetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import org.junit.Test;
import java.util.Properties;


/**
 *
 * @author jbuhacoff
 */
public class CreateTlsKeypairTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateTlsKeypairTest.class);
    
    @Test
    public void testCreateTlsKeypair() {
        Properties properties = new Properties();
        properties.setProperty(TrustagentConfiguration.TRUSTAGENT_KEYSTORE_PASSWORD,"password");
        PropertiesConfiguration configuration = new PropertiesConfiguration(properties);
        CreateTlsKeypair task = new CreateTlsKeypair();
        task.setConfiguration(configuration);
        if( task.isConfigured() ) {
            log.debug("Running task: {}", task.getClass().getName());
            task.run();
        }
        else {
            log.error("Configuration errors: {}", task.getConfigurationFaults());
            
        }
    }
}
