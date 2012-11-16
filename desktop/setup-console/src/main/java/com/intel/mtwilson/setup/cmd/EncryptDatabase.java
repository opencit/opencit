/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.setup.Command;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.SetupWizard;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class EncryptDatabase implements Command {

    @Override
    public void execute(String[] args) throws SetupException {
            Configuration attestationServiceConf = ASConfig.getConfiguration();
            SetupWizard wizard = new SetupWizard(attestationServiceConf);
            wizard.encryptVmwareConnectionStrings();
    }
    
}
