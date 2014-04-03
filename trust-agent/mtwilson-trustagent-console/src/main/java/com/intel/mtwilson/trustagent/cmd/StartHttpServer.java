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
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Command line should have -Dfs.root=/opt/trustagent and -Dfs.conf=/opt/trustagent/configuration
 * 
 * @author jbuhacoff
 */
public class StartHttpServer implements Command {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StartHttpServer.class);
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
        server = createServer();
        server.start();
        addShutdownHook();
        server.join();
    }
    
    protected Server createServer() {
        Server server = new Server();
        ServerConnector https = createTlsConnector(server);
        server.setConnectors(new Connector[] { https });
        server.setStopAtShutdown(true);
        WebAppContext webAppContext = createWebAppContext();
        server.setHandler(webAppContext);
        return server;
    }
    
    protected ServerConnector createTlsConnector(Server server) {
        HttpConfiguration httpsConfig = new HttpConfiguration();
//        httpConfig.setSecurePort(configuration.getTrustagentHttpTlsPort()); // only need on an http connection to inform client where to connect with https
        httpsConfig.setOutputBufferSize(32768);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(configuration.getTrustagentKeystoreFile().getAbsolutePath());
        sslContextFactory.setKeyStorePassword(configuration.getTrustagentKeystorePassword());
        //sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
        ServerConnector https = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory,"http/1.1"),
            new HttpConnectionFactory(httpsConfig));
        https.setPort(configuration.getTrustagentHttpTlsPort());
        https.setIdleTimeout(1000000);
        return https;
    }
    
    protected WebAppContext createWebAppContext() {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setDefaultsDescriptor(null); // disables JSP referenced by message "NO JSP Support for /, did not find org.apache.jasper.servlet.JspServlet" when starting jetty
//        webAppContext.setContextPath("/webapp");
//        webAppContext.setResourceBase("src/main/webapp");       
//        webAppContext.setClassLoader(getClass().getClassLoader());
        webAppContext.setResourceBase(MyFilesystem.getApplicationFilesystem().getBootstrapFilesystem().getHypertextPath());
        return webAppContext;
    }
    
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("Trust Agent Shutdown Hook") {
            @Override
            public void run() {
                try {
                    if (server != null) {
                        log.debug("Waiting for server to stop");
                        server.stop();
                    }
                } catch (Exception ex) {
                    log.error("Error stopping container: " + ex);
                }
            }
        });
    }
    
}
