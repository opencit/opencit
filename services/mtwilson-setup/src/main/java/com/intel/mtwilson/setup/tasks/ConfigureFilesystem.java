/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.io.Platform;
import com.intel.dcsg.cpg.validation.ObjectModel;
import com.intel.mtwilson.My;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.setup.ConfigurationException;
import com.intel.mtwilson.setup.SetupTask;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

/**
 * This task checks that required paths are known such as MTWILSON_HOME, and
 * MTWILSON_CONF. It is not able to configure them because it cannot set
 * environment variables for the user (well, under Linux we could detect a
 * ~/.profile and then add someting like . ~/mtwilson.env if that file exists,
 * and create that file if it doesn't exist with the required vars...)
 *
 * @author jbuhacoff
 */
public class ConfigureFilesystem extends AbstractSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigureFilesystem.class);
    private String mtwilsonHome;
    private String mtwilsonConf;

    private boolean checkFolderExists(String symbolicName, String path) {
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

    @Override
    protected void configure() throws Exception {
        mtwilsonHome = My.configuration().getMtWilsonHome();
        mtwilsonConf = My.configuration().getMtWilsonConf();
        if (mtwilsonHome == null) {
            configuration("MTWILSON_HOME is not configured");
        }
        if (mtwilsonConf == null) {
            configuration("MTWILSON_CONF is not configured");
        }
    }

    @Override
    protected void validate() throws Exception {
        checkFolderExists("MTWILSON_HOME", mtwilsonHome);
        checkFolderExists("MTWILSON_CONF", mtwilsonConf);
    }

    @Override
    protected void execute() throws Exception {
        if (Platform.isWindows()) {
            if (winHasSetx()) {
                // we can set the variable!
                runToVoid("setx MTWILSON_HOME " + mtwilsonHome);
                runToVoid("cmd /c mkdir " + mtwilsonHome);//  mkdir and set are shell commands not stand-alone executables, so if we don't prefix cmd /c we would get java.io.IOException: CreateProcess error=2, The system cannot find the file specified
                runToVoid("setx MTWILSON_CONF " + mtwilsonConf);
                runToVoid("cmd /c mkdir " + mtwilsonConf);//  mkdir and set are shell commands not stand-alone executables, so if we don't prefix cmd /c we would get java.io.IOException: CreateProcess error=2, The system cannot find the file specified
            }
        }
        if (Platform.isUnix()) {
        }
    }

    public static class Command {

        public String exec; // the command line to execute
        public byte[] output; // will contain the output when done
        // public byte[] input; // input to send to stdin of the command
    }

    private void runToVoid(String commandLine) throws IOException {
        log.debug("runToVoid: {}", commandLine);
        Process p = Runtime.getRuntime().exec(commandLine);
    }

    private String runToString(String commandLine) throws IOException {
        log.debug("runToString: {}", commandLine);
        Process p = Runtime.getRuntime().exec(commandLine);
        InputStream in = p.getInputStream();
        String output = IOUtils.toString(in);
        in.close();
        return output;
    }

    private boolean winHasSetx() throws IOException {
        String result = runToString("where setx");
        return result != null && !result.isEmpty();
    }
}
