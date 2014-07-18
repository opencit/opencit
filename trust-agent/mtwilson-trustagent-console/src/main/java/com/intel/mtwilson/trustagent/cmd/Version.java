/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.cmd;

import com.intel.dcsg.cpg.console.Command;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class Version implements Command {

    @Override
    public void setOptions(Configuration options) {
        // no options are needed for version command
    }

    @Override
    public void execute(String[] args) throws Exception {
        com.intel.mtwilson.Version version = com.intel.mtwilson.Version.getInstance();
        System.out.println("trustagent");
        System.out.println(String.format("Version %s", version.getVersion()));
        System.out.println(String.format("Build %s at %s", version.getBranch(), version.getTimestamp()));
    }
    
}
