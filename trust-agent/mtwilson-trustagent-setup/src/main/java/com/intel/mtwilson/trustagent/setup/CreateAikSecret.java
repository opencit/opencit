/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;

/**
 *
 * @author jbuhacoff
 */
public class CreateAikSecret extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateAikSecret.class);
    
    @Override
    protected void configure() throws Exception {
        TrustagentConfiguration trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        String aikSecret = trustagentConfiguration.getAikSecretHex();
        if( aikSecret == null || aikSecret.isEmpty() ) {
            aikSecret = RandomUtil.randomHexString(20);
            log.info("Generated random AIK secret"); 
            getConfiguration().setString(TrustagentConfiguration.AIK_SECRET, aikSecret);
        }
    }

    @Override
    protected void validate() throws Exception {
        TrustagentConfiguration trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        String aikSecret = trustagentConfiguration.getAikSecretHex();
        if( aikSecret == null || aikSecret.isEmpty() ) {
            validation("AIK secret is not set");
        }
    }

    @Override
    protected void execute() throws Exception {
        // nothing to do here, this setup task is only configuration
    }
    
}
