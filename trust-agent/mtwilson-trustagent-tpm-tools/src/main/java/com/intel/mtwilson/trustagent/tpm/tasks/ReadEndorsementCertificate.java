/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */

package com.intel.mtwilson.trustagent.tpm.tasks;

import com.intel.mtwilson.common.TAException;
import com.intel.mtwilson.trustagent.tpm.tools.TpmCommands;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.IOException;

/**
 *
 * @author rksavino
 */
public class ReadEndorsementCertificate {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReadEndorsementCertificate.class);
    private static TrustagentConfiguration configuration;

    public ReadEndorsementCertificate() throws IOException {
        this.configuration = TrustagentConfiguration.loadConfiguration();
    }
    
    public static String getEndorsementIdentifier() throws TAException, IOException {
        String ret = TpmCommands.tpmNvread("0x10d00f000", 64, configuration.getTpmOwnerSecretHex());
        System.out.println("return value: \r\n" + ret);
        return ret;
    }
}
