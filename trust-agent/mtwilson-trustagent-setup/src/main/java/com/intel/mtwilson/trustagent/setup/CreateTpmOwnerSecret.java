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
public class CreateTpmOwnerSecret extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateTpmOwnerSecret.class);
    
    @Override
    protected void configure() throws Exception {
    }

    @Override
    protected void validate() throws Exception {
        TrustagentConfiguration trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        String tpmOwnerSecretHex = trustagentConfiguration.getTpmOwnerSecretHex();
        if( tpmOwnerSecretHex == null || tpmOwnerSecretHex.isEmpty() ) {
            validation("TPM owner secret is not set");
        }
    }

    @Override
    protected void execute() throws Exception {
        String tpmOwnerSecretHex = RandomUtil.randomHexString(20);
        log.info("Generated random owner secret"); 
        getConfiguration().setString(TrustagentConfiguration.TPM_OWNER_SECRET, tpmOwnerSecretHex);
    }
    
}
