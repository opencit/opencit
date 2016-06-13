/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.niarl.Util;
import com.intel.mtwilson.trustagent.tpmmodules.Tpm;
import gov.niarl.his.privacyca.IdentityOS;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmUtils;

/**
 * 
 * @author jbuhacoff
 */
public class TakeOwnership extends AbstractSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TakeOwnership.class);
    private TrustagentConfiguration config;
    private String tpmOwnerSecret;

    @Override
    protected void configure() throws Exception {
        // tpm owner password must have already been generated
        config = new TrustagentConfiguration(getConfiguration());
        tpmOwnerSecret = config.getTpmOwnerSecretHex();
//        log.debug("TakeOwnership tpmOwnerSecret = {}", tpmOwnerSecret);
        if (tpmOwnerSecret == null || tpmOwnerSecret.isEmpty()) {
            configuration("TPM owner secret must be configured to take ownership");
        }
    }

    @Override
    protected void validate() throws Exception {
        if (!Util.isOwner(config.getTpmOwnerSecret())) {
            validation("Trust Agent is not the TPM owner");
        }
    }

    @Override
    protected void execute() throws Exception {
        // Take Ownership
        if (IdentityOS.isWindows()) { 
            /* return for now since Windows usually take the ownership of TPM be default 
             * need to check later for exceptions
            */
            return;
        } 
        else { /* for Linux TPM 1.2 and TPM 2.0 */
            byte[] nonce1 = TpmUtils.createRandomBytes(20);            
            try {
                Tpm.getModule().takeOwnership(config.getTpmOwnerSecret(), nonce1);
            } catch (TpmModule.TpmModuleException e) {
                if( e.getErrorCode() != null && e.getErrorCode() == 4 ) {
                    log.info("Ownership is already taken");
                }
                else {
                    throw e;
                }
            }
        }
    }
}