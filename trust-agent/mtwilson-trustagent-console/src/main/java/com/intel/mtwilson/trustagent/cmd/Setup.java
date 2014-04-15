/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.MyFilesystem;
import com.intel.mtwilson.setup.console.cmd.SetupManager;
import java.io.File;

/**
 * Command line should have -Dfs.root=/opt/trustagent and -Dfs.conf=/opt/trustagent/configuration
 * 
 * @author jbuhacoff
 */
public class Setup extends SetupManager implements Command {

    @Override
    protected File getConfigurationFile() {
        File file = new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "trustagent.properties");
        return file;
    }


}
