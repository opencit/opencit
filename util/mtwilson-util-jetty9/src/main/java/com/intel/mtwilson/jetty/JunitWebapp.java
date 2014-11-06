/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author jbuhacoff
 */
public class JunitWebapp {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JunitWebapp.class);

    private static Server server;
    
    @BeforeClass
    public static void start() throws Exception {
        server = new Server(8080);
        server.setStopAtShutdown(true);
        WebAppContext webAppContext = new WebAppContext();
//        webAppContext.setContextPath("/webapp");
        webAppContext.setResourceBase("src/main/webapp");       
//        webAppContext.setClassLoader(getClass().getClassLoader());
        server.setHandler(webAppContext);
        server.start();        
    }
    
    @AfterClass
    public static void stop() throws Exception {
        server.stop();
    }
}
