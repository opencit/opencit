/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.RemoteSetup;
import com.intel.mtwilson.setup.SetupContext;
import com.intel.mtwilson.setup.Timeout;
import com.intel.mtwilson.setup.model.SetupTarget;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.userauth.UserAuthException;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class ConfigureLocal implements Command {
  

    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws Exception {

//        ctx.target = SetupTarget.LOCAL;
        

    }
    
}
