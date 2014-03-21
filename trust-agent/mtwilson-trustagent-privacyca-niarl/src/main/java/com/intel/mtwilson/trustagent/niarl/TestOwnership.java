/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.niarl;

import com.intel.mtwilson.My;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import gov.niarl.his.privacyca.TpmModule;
import java.io.IOException;

/**
 * Test TPM ownership status by attempting to change the SRK secret and then
 * (if successful) change it back to its original value:
 * 
 * hOldSecret = current SRK secret;
 * Tspi_ChangeAuth(hSRK, hTPM, hNewSecret); 
 * Tspi_ChangeAuth(hSRK, hTPM, hOldSecret);
 * 
 * @author jbuhacoff
 */
public class TestOwnership {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestOwnership.class);
    
    public boolean isOwner() {
        try {
            TrustagentConfiguration config = new TrustagentConfiguration(My.configuration().getConfiguration());
            TpmModule.getCredential(config.getTpmOwnerSecret(), "EC");
            return true;
        }
        catch(IOException | TpmModule.TpmModuleException e) {
            log.error("Ownership test failed: {}", e);
            return false;
        }
    }
}
