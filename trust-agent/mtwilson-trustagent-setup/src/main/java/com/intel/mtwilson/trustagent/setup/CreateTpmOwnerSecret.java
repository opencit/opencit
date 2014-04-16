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
    
    private String tpmOwnerSecretHex;
    private String tpmSrkSecretHex;
    
    @Override
    protected void configure() throws Exception {
        TrustagentConfiguration trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        tpmOwnerSecretHex = trustagentConfiguration.getTpmOwnerSecretHex();
        if( tpmOwnerSecretHex == null || tpmOwnerSecretHex.isEmpty() ) {
            tpmOwnerSecretHex = RandomUtil.randomHexString(20);
            log.info("Generated random owner secret"); 
        }
        tpmSrkSecretHex = trustagentConfiguration.getTpmSrkSecretHex();
        if( tpmSrkSecretHex == null || tpmSrkSecretHex.isEmpty() ) {
            tpmSrkSecretHex = "0000000000000000000000000000000000000000"; // to match existing ProvisionTPM hard-coded value;  TODO: change to RandomUtil.randomHexString(20) after verifying rest of stack can handle well-known SRK ... well-known SRK may be a bad assumption, but at least now we have an option to use a configured value instead;  see issue #1012
            log.info("Generated well-known SRK secret"); 
        }
    }

    @Override
    protected void validate() throws Exception {
        if( tpmOwnerSecretHex == null || tpmOwnerSecretHex.isEmpty() ) {
            validation("TPM owner secret is not set");
        }
        if( tpmSrkSecretHex == null || tpmSrkSecretHex.isEmpty() ) {
            validation("TPM SRK secret is not set");
        }
    }

    @Override
    protected void execute() throws Exception {
        getConfiguration().setString(TrustagentConfiguration.TPM_OWNER_SECRET, tpmOwnerSecretHex);
        getConfiguration().setString(TrustagentConfiguration.TPM_SRK_SECRET, tpmSrkSecretHex);
    }
    
}
