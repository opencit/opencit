/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */

package com.intel.mtwilson.trustagent.tpm.tools;

import com.intel.dcsg.cpg.codec.HexUtil;
import com.intel.mtwilson.common.CommandResult;
import com.intel.mtwilson.common.CommandUtil;
import com.intel.mtwilson.common.TAException;
import java.io.IOException;

/**
 *
 * @author rksavino
 */
public class TpmCommands {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmCommands.class);
    
    public static String tpmNvread(String index, Integer size, String ownerPassword) throws TAException, IOException {
        try {
            if (!HexUtil.isHex(ownerPassword)) {
                log.error("OWNER_PASSWORD is not in hex format: {}", ownerPassword);
                throw new IllegalArgumentException("OWNER_PASSWORD is not in hex format.");
            }
            log.debug("running command tpm_nvread -x -t -i " + index + " -s " + size + " -pOWNER_PASSWORD");
            String[] variables = { "OWNER_PASSWORD=" + ownerPassword };
            CommandResult result = CommandUtil.runCommand("tpm_nvread -x -t -i " + index + " -s " + size + " -pOWNER_PASSWORD", variables);
            if (result.getExitcode() == 0) {
                return result.getStdout();
            } else {
                log.error("TPM read error: {}", result.getStderr());
                throw new IOException(String.format("Error reading nvram."));
            }
        }catch(TAException ex) {
                log.error("Error reading from nvram: {}", ex.getMessage());
                throw ex;
        }
    }
}
