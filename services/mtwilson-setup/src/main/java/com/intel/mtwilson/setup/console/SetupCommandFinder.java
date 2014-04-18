/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.console;

import com.intel.dcsg.cpg.console.Command;
import com.intel.dcsg.cpg.console.CommandFinder;
import com.intel.dcsg.cpg.console.HyphenatedCommandFinder;
import java.util.HashMap;

/**
 *
 * @author jbuhacoff
 */
public class SetupCommandFinder implements CommandFinder {
    private final HyphenatedCommandFinder finder;
    
    public SetupCommandFinder() {
        HashMap<String,String> map = new HashMap<>();
        map.put("mtwilson", "MtWilson");
        finder = new HyphenatedCommandFinder("com.intel.mtwilson.setup.console.cmd", map);
    }
    
    @Override
    public Command forName(String commandName) {
        return finder.forName(commandName);
    }
}
