/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.MyFilesystem;
import com.intel.mtwilson.setup.console.cmd.SetupManager;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.util.Properties;

/**
 * Command line should have -Dfs.root=/opt/trustagent and -Dfs.conf=/opt/trustagent/configuration
 * 
 * @author jbuhacoff
 */
public class Setup extends SetupManager implements Command {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Setup.class);
    
    @Override
    protected File getConfigurationFile() {
        File file = new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "trustagent.properties");
        return file;
    }

    @Override
    protected Properties beforeStore(Properties properties) {
        Properties copy = copy(properties);
        copy.remove(TrustagentConfiguration.MTWILSON_API_USERNAME);
        copy.remove(TrustagentConfiguration.MTWILSON_API_PASSWORD);
        return copy;
    }

    private Properties copy(Properties given) {
        Properties copy = new Properties();
        for(String key : given.stringPropertyNames()) {
            String value = given.getProperty((String)key);
            copy.setProperty(key, value);
        }
        return copy;
    }

}
