/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */

package com.intel.mtwilson.trustagent.tpm.tools;

import com.intel.mtwilson.codec.HexUtil;
import com.intel.mtwilson.common.TAException;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.exec.CommandLine;

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
            Map<String, String> variables = new HashMap<>();
            variables.put("OWNER_PASSWORD", ownerPassword);
            CommandLine command = new CommandLine("/opt/trustagent/bin/tpm_nvread");
            command.addArgument("-x");
            command.addArgument("-t");
            command.addArgument(String.format("-i %s", index), false);
            command.addArgument(String.format("-s %s", size), false);
            command.addArgument("-pOWNER_PASSWORD");
            Result result = ExecUtil.execute(command, variables);
            if (result.getExitCode() == 0) {
                return result.getStdout();
            } else {
                log.error("TPM read error: {}", result.getStderr());
                throw new IOException(String.format("Error reading nvram."));
            }
        }catch(Exception ex) {
                log.error("Error reading from nvram: {}", ex.getMessage());
                throw ex;
        }
    }
}
