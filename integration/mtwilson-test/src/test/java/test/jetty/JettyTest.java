/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class duplicates test code from mtwilson-api-rest-api-v2: com.intel.mtwilson.ws.jersey.JerseyTest in test sources.
 * 
 * @author jbuhacoff
 */
public class JettyTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JettyTest.class);

    public static Server server = new Server(3700);

    @BeforeClass
    public static void start() throws Exception {

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
/*
        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/api/*"); // XXX maybe move the static resources to something other than /*  so i can put this at /* ?
        jerseyServlet.setInitOrder(1);
        jerseyServlet.setInitParameter(ServerProperties.APPLICATION_NAME, "mtwilson-api-v2"); // constant, might be seen if monitoring the server with JMX and useful for identifying threads of two or more applications running in the same container
        jerseyServlet.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "com.intel.mtwilson.ws.jersey,com.intel.mtwilson.ws.jersey.util,com.fasterxml.jackson.jaxrs.json,com.fasterxml.jackson.jaxrs.xml"); // XXX package names would come from plugin components and would be combined to form this property ... providers and resources found in these packages are ADDED to what is returned by getClasses() in the JerseyApplication instance 
        jerseyServlet.setInitParameter(ServerProperties.MEDIA_TYPE_MAPPINGS, "txt:text/plain, xml:application/xml, json:application/json, html:text/html"); // XXX need to combine mappings nominated by each plugin component , and have a rule about how to handle conflicts, or have this be an item that is controlled in central configuration where plugins can propose changes and administrator can resolve conflicts and save whatever they want here
        jerseyServlet.setInitParameter(ServerProperties.LANGUAGE_MAPPINGS, "en:en, fr:fr"); // XXX this one is more standard so we can simply convert an exsiting language code list to this format,  where the extension is the language code,  and optionally support some country codes too like .en_US -> en-US  


        // optional static content
        ServletHolder staticServlet = context.addServlet(DefaultServlet.class, "/*");
        staticServlet.setInitParameter("resourceBase", "src/main/resources/html5");
        staticServlet.setInitParameter("pathInfoOnly", "true");
*/

        ServletContainer jerseyServletContainer = new org.glassfish.jersey.servlet.ServletContainer();
        ServletHolder jerseyServletHolder = new ServletHolder(jerseyServletContainer); // XXX maybe move the static resources to something other than /*  so i can put this at /* ?
        jerseyServletHolder.setInitOrder(1);
        jerseyServletHolder.setInitParameter(ServerProperties.APPLICATION_NAME, "mtwilson-api-v2"); // constant, might be seen if monitoring the server with JMX and useful for identifying threads of two or more applications running in the same container
        jerseyServletHolder.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "com.intel.mtwilson.ws.jersey,com.intel.mtwilson.ws.jersey.util,com.fasterxml.jackson.jaxrs.json,com.fasterxml.jackson.jaxrs.xml"); // XXX package names would come from plugin components and would be combined to form this property ... providers and resources found in these packages are ADDED to what is returned by getClasses() in the JerseyApplication instance 
        jerseyServletHolder.setInitParameter(ServerProperties.MEDIA_TYPE_MAPPINGS, "txt:text/plain, xml:application/xml, json:application/json, html:text/html, saml:application/saml+xml, bin:application/octet-stream, yaml:text/yaml"); // XXX need to combine mappings nominated by each plugin component , and have a rule about how to handle conflicts, or have this be an item that is controlled in central configuration where plugins can propose changes and administrator can resolve conflicts and save whatever they want here;   NOTE: using text/yaml and not application/yaml as the default mapping for .yaml because browsers display text/yaml and try to download as a file application/yaml
        jerseyServletHolder.setInitParameter(ServerProperties.LANGUAGE_MAPPINGS, "en:en, fr:fr"); // XXX this one is more standard so we can simply convert an exsiting language code list to this format,  where the extension is the language code,  and optionally support some country codes too like .en_US -> en-US  
        context.addServlet(jerseyServletHolder, "/api/v2/*");

        // optional static content
        ServletHolder staticServletHolder = new ServletHolder(new DefaultServlet());
        staticServletHolder.setInitParameter("resourceBase", "src/main/resources/html5");
        staticServletHolder.setInitParameter("pathInfoOnly", "true");
        context.addServlet(staticServletHolder, "/*");
        
        server.setHandler(context);
        server.start();

        /*
        ServletHandler handler = new ServletHandler();
//        handler.set
        
        ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        context.setResourceBase(".");
        context.setClassLoader(Thread.currentThread().getContextClassLoader()); // XXX TODO for plugin arch this is important to get the right classloader for the module...
//        context.setHandler(new HelloHandler());
//        server.setHandler(context);  
* */
    }

    @AfterClass
    public static void stop() throws Exception {
        System.in.read(); // pause
        server.stop();
    }
    
    @Test
    public void testJetty() {
        // it's paused so try stuff in the browser:   http://localhost:3700/api/v2/hosts.json  
        log.debug("jetty.home = {}", System.getProperty("jetty.home")); // null
    }
        
}
