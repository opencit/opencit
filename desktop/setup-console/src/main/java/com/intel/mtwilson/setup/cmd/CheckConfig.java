/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.My;
import com.intel.mtwilson.setup.Command;
import com.intel.mtwilson.setup.RemoteSetup;
import com.intel.mtwilson.setup.SetupContext;
import com.intel.mtwilson.setup.Timeout;
import com.intel.mtwilson.setup.model.SetupTarget;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.userauth.UserAuthException;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class CheckConfig implements Command {
    private SetupContext ctx = null;

    @Override
    public void setContext(SetupContext ctx) {
        this.ctx = ctx;
    }

    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws Exception {

        ctx.target = SetupTarget.LOCAL; // hmm...  
        
            Properties p = My.persistenceManager().getASDataJpaProperties(ASConfig.getConfiguration());
        
        System.out.println("javax.persistence.jdbc.driver = "+p.getProperty("javax.persistence.jdbc.driver"));
        System.out.println("javax.persistence.jdbc.url = "+p.getProperty("javax.persistence.jdbc.url"));
        System.out.println("javax.persistence.jdbc.host = "+p.getProperty("javax.persistence.jdbc.host"));
        System.out.println("javax.persistence.jdbc.port = "+p.getProperty("javax.persistence.jdbc.port"));
        System.out.println("javax.persistence.jdbc.schema = "+p.getProperty("javax.persistence.jdbc.schema"));
        System.out.println("javax.persistence.jdbc.user = "+p.getProperty("javax.persistence.jdbc.user"));
        System.out.println("javax.persistence.jdbc.password = "+p.getProperty("javax.persistence.jdbc.password"));

    }
    
}
