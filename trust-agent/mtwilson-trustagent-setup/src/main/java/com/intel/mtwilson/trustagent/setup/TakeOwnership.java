/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.niarl.TestOwnership;
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
        if (tpmOwnerSecret == null || tpmOwnerSecret.isEmpty()) {
            configuration("TPM owner secret must be configured to take ownership");
        }
    }

    @Override
    protected void validate() throws Exception {
        try {
            TestOwnership test = new TestOwnership();
            if (!test.isOwner(config.getTpmOwnerSecret())) {
                validation("Trust Agent is not the TPM owner");
            }
        } catch (Exception e) {
            log.error("TPM ownership test failed: {}", e);
            validation(e, "TPM ownership test failed");
        }
    }

    @Override
    protected void execute() throws Exception {
        // Take Ownership
        byte[] nonce1 = TpmUtils.createRandomBytes(20);
        try {
            TpmModule.takeOwnership(config.getTpmOwnerSecret(), nonce1);
        } catch (TpmModule.TpmModuleException e) {
            if (e.toString().contains(".takeOwnership returned nonzero error: 4")) {
                log.info("Ownership is already taken");
            } else {
                throw e;
            }
        }
    }
}
