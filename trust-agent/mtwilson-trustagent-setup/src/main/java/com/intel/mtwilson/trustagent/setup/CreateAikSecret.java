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
    }

    @Override
    protected void validate() throws Exception {
        TrustagentConfiguration trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        String aikSecretHex = trustagentConfiguration.getAikSecretHex();
        if( aikSecretHex == null || aikSecretHex.isEmpty() ) {
            validation("AIK secret is not set");
        }
    }

    @Override
    protected void execute() throws Exception {
        String aikSecretHex = RandomUtil.randomHexString(20);
        log.info("Generated random AIK secret"); 
        getConfiguration().set(TrustagentConfiguration.AIK_SECRET, aikSecretHex);
    }
    
}
