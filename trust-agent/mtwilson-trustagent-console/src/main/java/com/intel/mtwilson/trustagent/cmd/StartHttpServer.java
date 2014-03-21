/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyFilesystem;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.security.Security;
import org.apache.commons.configuration.Configuration;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Command line should have -Dfs.root=/opt/trustagent and -Dfs.conf=/opt/trustagent/configuration
 * 
 * @author jbuhacoff
 */
public class StartHttpServer implements Command {
    private Server server;
    private TrustagentConfiguration configuration;
    private Configuration options;
    
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws Exception {
        configuration = new TrustagentConfiguration(My.configuration().getConfiguration());
        Security.addProvider(new BouncyCastleProvider());
        start();
    }
    
    public void start() throws Exception {
        server = new Server(configuration.getTrustagentHttpPort());
        server.setStopAtShutdown(true);
        WebAppContext webAppContext = new WebAppContext();
//        webAppContext.setContextPath("/webapp");
//        webAppContext.setResourceBase("src/main/webapp");       
//        webAppContext.setClassLoader(getClass().getClassLoader());
        webAppContext.setResourceBase(MyFilesystem.getApplicationFilesystem().getBootstrapFilesystem().getHypertextPath());
        server.setHandler(webAppContext);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

}
