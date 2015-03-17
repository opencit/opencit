/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.io.Platform;
import com.intel.dcsg.cpg.validation.ObjectModel;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.My;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.setup.ConfigurationException;
import com.intel.mtwilson.setup.LocalSetupTask;
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
public class ConfigureFilesystem extends LocalSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigureFilesystem.class);
    private String mtwilsonHome;
    private String mtwilsonConf;

    @Override
    protected void configure() throws Exception {
        mtwilsonHome = Folders.application(); //My.filesystem().getApplicationPath(); //My.configuration().getMtWilsonHome();
        mtwilsonConf = My.configuration().getDirectoryPath(); // My.filesystem().getConfigurationPath(); //My.configuration().getMtWilsonConf();
        if (mtwilsonHome == null) {
            configuration("MTWILSON_HOME is not configured");
        }
        if (mtwilsonConf == null) {
            configuration("MTWILSON_CONF is not configured");
        }
    }

    @Override
    protected void validate() throws Exception {
        checkFileExists("MTWILSON_HOME", mtwilsonHome);
        checkFileExists("MTWILSON_CONF", mtwilsonConf);
    }

    @Override
    protected void execute() throws Exception {
        if (Platform.isWindows()) {
            if (winHasSetx()) {
                // we can set the variable!
                runToVoid("setx MTWILSON_HOME " + mtwilsonHome);
                runToVoid("setx MTWILSON_CONF " + mtwilsonConf);
            }
            runToVoid("cmd /c mkdir " + mtwilsonHome);//  mkdir and set are shell commands not stand-alone executables, so if we don't prefix cmd /c we would get java.io.IOException: CreateProcess error=2, The system cannot find the file specified
            runToVoid("cmd /c mkdir " + mtwilsonConf);//  mkdir and set are shell commands not stand-alone executables, so if we don't prefix cmd /c we would get java.io.IOException: CreateProcess error=2, The system cannot find the file specified
        }
        if (Platform.isUnix()) {
            runToVoid("mkdir -p "+mtwilsonHome);
            runToVoid("mkdir -p "+mtwilsonConf);
        }
    }

    private boolean winHasSetx() throws IOException {
        String result = runToString("where setx");
        return result != null && !result.isEmpty();
    }
}
