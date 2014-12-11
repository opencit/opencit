/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

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
        // in order to force create-tpm-owner-secret to generate a new password,
        // you have to delete the existing password from the configuration or
        // clear the tpm.
        TrustagentConfiguration trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        String existingTpmOwnerSecretHex = trustagentConfiguration.getTpmOwnerSecretHex();
        if( isTpmOwned() && existingTpmOwnerSecretHex != null && !existingTpmOwnerSecretHex.isEmpty() ) {
            // tpm is owned and there's already a password set so we skip creating
            // a new password even if --force was provided because it would just
            // break things. 
            log.info("Ownership is already taken and password is already set");
            return;
        }
        // otherwise we would create a new password here and it would be useless
        // if the tpm is already owned.
        String tpmOwnerSecretHex = RandomUtil.randomHexString(20);
        log.info("Generated random owner secret"); 
        getConfiguration().set(TrustagentConfiguration.TPM_OWNER_SECRET, tpmOwnerSecretHex);
    }
    
    private boolean isTpmOwned() throws IOException {
        File tpmOwned = new File("/sys/class/misc/tpm0/device/owned");
        String text = FileUtils.readFileToString(tpmOwned); // "1" or "0"
        Integer number = Integer.valueOf(text.trim());
        return number == 1;
    }
}
