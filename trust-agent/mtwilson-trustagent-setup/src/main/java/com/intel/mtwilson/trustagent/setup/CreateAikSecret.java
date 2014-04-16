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
    
    private String aikSecretHex;
    
    @Override
    protected void configure() throws Exception {
        TrustagentConfiguration trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        aikSecretHex = trustagentConfiguration.getAikSecretHex();
        if( aikSecretHex == null || aikSecretHex.isEmpty() ) {
            aikSecretHex = RandomUtil.randomHexString(20);
            log.info("Generated random AIK secret"); 
        }
    }

    @Override
    protected void validate() throws Exception {
        if( aikSecretHex == null || aikSecretHex.isEmpty() ) {
            validation("AIK secret is not set");
        }
    }

    @Override
    protected void execute() throws Exception {
        getConfiguration().setString(TrustagentConfiguration.AIK_SECRET, aikSecretHex);
    }
    
}
