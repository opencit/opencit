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
        TrustagentConfiguration trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        String tpmOwnerSecret = trustagentConfiguration.getTpmOwnerSecretHex();
        if( tpmOwnerSecret == null || tpmOwnerSecret.isEmpty() ) {
            tpmOwnerSecret = RandomUtil.randomHexString(20);
            log.info("Generated random owner secret"); 
            getConfiguration().setString(TrustagentConfiguration.TPM_OWNER_SECRET, tpmOwnerSecret);
        }
        String tpmSrkSecret = trustagentConfiguration.getTpmSrkSecretHex();
        if( tpmSrkSecret == null || tpmSrkSecret.isEmpty() ) {
            tpmSrkSecret = "0000000000000000000000000000000000000000"; // to match existing ProvisionTPM hard-coded value;  TODO: change to RandomUtil.randomHexString(20) after verifying rest of stack can handle well-known SRK ... well-known SRK may be a bad assumption, but at least now we have an option to use a configured value instead;  see issue #1012
            log.info("Generated well-known SRK secret"); 
            getConfiguration().setString(TrustagentConfiguration.TPM_SRK_SECRET, tpmSrkSecret);
        }
    }

    @Override
    protected void validate() throws Exception {
        TrustagentConfiguration trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        String tpmOwnerSecret = trustagentConfiguration.getTpmOwnerSecretHex();
        if( tpmOwnerSecret == null || tpmOwnerSecret.isEmpty() ) {
            validation("TPM owner secret is not set");
        }
        String tpmSrkSecret = trustagentConfiguration.getTpmSrkSecretHex();
        if( tpmSrkSecret == null || tpmSrkSecret.isEmpty() ) {
            validation("TPM SRK secret is not set");
        }
    }

    @Override
    protected void execute() throws Exception {
        // nothing to do here, this setup task is only configuration
    }
    
}
