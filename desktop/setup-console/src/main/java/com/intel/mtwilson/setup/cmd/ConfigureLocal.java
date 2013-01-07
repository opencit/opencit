/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mtwilson.setup.Command;
import com.intel.mtwilson.setup.RemoteSetup;
import com.intel.mtwilson.setup.SetupContext;
import com.intel.mtwilson.setup.Timeout;
import com.intel.mtwilson.setup.model.SetupTarget;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.userauth.UserAuthException;

/**
 *
 * @author jbuhacoff
 */
public class ConfigureLocal implements Command {
    private SetupContext ctx = null;

    @Override
    public void setContext(SetupContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void execute(String[] args) throws Exception {

        ctx.target = SetupTarget.LOCAL;
        

    }
    
}
