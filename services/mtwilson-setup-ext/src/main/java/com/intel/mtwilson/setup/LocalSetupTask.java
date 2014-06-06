/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

/**
 * Adds convenience methods for filesystem tasks
 * 
 * @author jbuhacoff
 */
public abstract class LocalSetupTask extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocalSetupTask.class);
    
    protected boolean checkFileExists(String symbolicName, String path) {
        if (path == null) {
            configuration("%s is not configured", symbolicName);
            return false;
        }
        File folder = new File(path);
        if (!folder.exists()) {
            validation("%s (%s) does not exist", symbolicName, path);
            return false;
        }
        return true;
    }

    public static class Command {

        public String exec; // the command line to execute
        public byte[] output; // will contain the output when done
        // public byte[] input; // input to send to stdin of the command
    }

    protected void runToVoid(String commandLine) throws IOException {
        log.debug("runToVoid: {}", commandLine);
        Runtime.getRuntime().exec(commandLine);
    }

    protected String runToString(String commandLine) throws IOException {
        log.debug("runToString: {}", commandLine);
        Process p = Runtime.getRuntime().exec(commandLine);
        String output;
        try (InputStream in = p.getInputStream()) {
            output = IOUtils.toString(in);
        }
        return output;
    }

    
}
