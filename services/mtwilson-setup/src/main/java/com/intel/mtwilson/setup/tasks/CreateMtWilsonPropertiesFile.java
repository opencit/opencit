/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.mtwilson.My;
import com.intel.mtwilson.setup.LocalSetupTask;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Depends on ConfigureFilesystem. 
 * @author jbuhacoff
 */
public class CreateMtWilsonPropertiesFile extends LocalSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateMtWilsonPropertiesFile.class);
    private String mtwilsonConf; // optional input
    private transient File mtwilsonProperties; // not an input;  path relative to mtwilsonConf is hard-coded 

    @Override
    protected void configure() throws Exception {
        mtwilsonConf = My.configuration().getDirectoryPath(); //My.filesystem().getConfigurationPath(); //My.configuration().getMtWilsonConf();
        if (mtwilsonConf == null) {
            configuration("MTWILSON_CONF is not configured");
        }
        // we don't store MTWILSON_CONF in the configuration because it's needed to load the configuration itself
    }

    @Override
    protected void validate() throws Exception {
        mtwilsonProperties = new File(mtwilsonConf + File.separator + "mtwilson.properties");
        checkFileExists("MTWILSON_CONF", mtwilsonConf);
        checkFileExists("mtwilson.properties", mtwilsonProperties.getAbsolutePath());
    }

    @Override
    protected void execute() throws Exception {
        try (FileOutputStream out = new FileOutputStream(mtwilsonProperties)) {
            Properties properties = new Properties();
            properties.store(out, "automatically generated");
        }
    }

}
