/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.dcg.console.Command;
import com.intel.dcg.console.CommandFinder;
import com.intel.dcg.console.PackageCommandFinder;

/**
 *
 * @author jbuhacoff
 */
public class MWCommandFinder implements CommandFinder {
    private PackageCommandFinder finder = new PackageCommandFinder("com.intel.mtwilson.setup.cmd");
    
    @Override
    public Command forName(String commandName) {
        return finder.forName(commandName);
    }
}
