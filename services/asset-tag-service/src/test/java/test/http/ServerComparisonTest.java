/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.http;

import com.intel.mtwilson.My;
import com.intel.mtwilson.atag.RestletApplication;
import com.intel.mtwilson.atag.client.At;
import java.net.URL;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.jetty.HttpServerHelper;
import org.restlet.ext.jetty.JettyServerHelper;
import org.restlet.resource.ClientResource;

/**
 * This class tests the Restlet internal HTTP connector.  When we use it to run the
 * web service it seems to serve only a few requests and then says it's too busy and
 * stops responding to requests.
 * 
 * @author jbuhacoff
 */
public class ServerComparisonTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ServerComparisonTest.class);
    private static int max = 50;
    
    private static int getPort(URL url) {
        int port = url.getPort();
        if( port == - 1 ) {
            if( "http".equals(url.getProtocol()) ) {
                port = 80;
            }
            else if( "https".equals(url.getProtocol())) {
                port = 443;
            }
        }
        return port;
    }
    
    @Test
    public void testRestletDefaultServer() throws Exception {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, getPort(My.configuration().getAssetTagServerURL()));
//        component.getServers().add(Protocol.FILE);,  // apparently this one is not required for the Directory() resource
        component.getClients().add(Protocol.FILE); //  required to enable the Directory() resource defined in RestletApplication... dont' know why it has to be added to the clients, unless when we say "file://..." we are acting as a client of a file system resource  internally ???
        component.getClients().add(Protocol.CLAP); //  required to enable the Directory() resource defined in RestletApplication... dont' know why it has to be added to the clients, unless when we say "file://..." we are acting as a client of a file system resource  internally ???
        component.getDefaultHost().attach("", new RestletApplication()); // if the restlet attaches to "/fruit", this must be "", not "/";  but if the restlet attaches to "fruit", then this can be "/"
        component.start();
        long start = System.currentTimeMillis();
        // test a bunch of "get tag" requests:
        for(int i=0; i<max; i++) {
            /*
            log.debug("Requesting GET /tags # {}", i);
//            String existingTags = At.tags(.get(String.class);
            ClientResource tag = At.tags();
            String existingTags = tag.get(String.class);
            tag.release();
//            tag.getResponse().getEntity().exhaust();
            log.debug("Tags: {}", existingTags);
            */
            log.debug("Requesting GET /selections # {}", i);
//            String existingSelections = At.selections().get(String.class);
            ClientResource selection = At.selections();
            String existingSelections = selection.get(String.class);
            selection.release();
            log.debug("Selections: {}", existingSelections);
            /*
            log.debug("Requesting GET /rdf-triples # {}", i);
            ClientResource rdfTriple = At.rdfTriples();
            String existingRdf = rdfTriple.get(String.class);
            log.debug("RDF Triples: {}", existingRdf);
            rdfTriple.release();
            log.debug("Released RDF triple");
            */
        }
        long end = System.currentTimeMillis();
        log.debug("Restlet default server time: {} ms", end-start);
        component.stop();
    }
    
    /**
     * References:
     * http://restlet.org/learn/javadocs/2.1/jse/ext/org/restlet/ext/jetty/JettyServerHelper.html
     * http://restlet.org/learn/guide/2.0/extensions/jetty/ajp
     * 
     * @throws Exception 
     */
    @Test
    public void testJettyServer() throws Exception {
        Component component = new Component();
        component.getClients().add(Protocol.FILE); //  required to enable the Directory() resource defined in RestletApplication... dont' know why it has to be added to the clients, unless when we say "file://..." we are acting as a client of a file system resource  internally ???
        component.getClients().add(Protocol.CLAP); //  required to enable the Directory() resource defined in RestletApplication... dont' know why it has to be added to the clients, unless when we say "file://..." we are acting as a client of a file system resource  internally ???
        component.getDefaultHost().attach("", new RestletApplication()); // if the restlet attaches to "/fruit", this must be "", not "/";  but if the restlet attaches to "fruit", then this can be "/"
        component.getContext().getAttributes().put("minThreads", 10);
        
        Server server=new Server(
                component.getContext(),
                Protocol.HTTP,
                getPort(My.configuration().getAssetTagServerURL()),
                component
            );        
        JettyServerHelper jetty = new HttpServerHelper(server);

        jetty.start();
        long start = System.currentTimeMillis();
        // test a bunch of "get tag" requests:
        for(int i=0; i<max; i++) {
            log.debug("Requesting GET /tags # {}",i);
            String existingTags = At.tags().get(String.class);
            log.debug("Tags: {}", existingTags);
            log.debug("Requesting GET /selections # {}",i);
            String existingSelections = At.selections().get(String.class);
            log.debug("Selections: {}", existingSelections);
        }
        long end = System.currentTimeMillis();
        log.debug("Jetty server time: {} ms", end-start);
        jetty.stop();
        
    }

    @Test
    public void testGrizzlyServer() throws Exception {
        if(true) { return; } // grizzly component configuration not implemented yet ... below is a copy of the jetty config.
        Component component = new Component();
        component.getClients().add(Protocol.FILE); //  required to enable the Directory() resource defined in RestletApplication... dont' know why it has to be added to the clients, unless when we say "file://..." we are acting as a client of a file system resource  internally ???
        component.getClients().add(Protocol.CLAP); //  required to enable the Directory() resource defined in RestletApplication... dont' know why it has to be added to the clients, unless when we say "file://..." we are acting as a client of a file system resource  internally ???
        component.getDefaultHost().attach("", new RestletApplication()); // if the restlet attaches to "/fruit", this must be "", not "/";  but if the restlet attaches to "fruit", then this can be "/"
        component.getContext().getAttributes().put("minThreads", 10);
        
        Server server=new Server(
                component.getContext(),
                Protocol.HTTP,
                getPort(My.configuration().getAssetTagServerURL()),
                component
            );        
        JettyServerHelper jetty = new HttpServerHelper(server);

        jetty.start();
        long start = System.currentTimeMillis();
        // test a bunch of "get tag" requests:
        for(int i=0; i<max; i++) {
            log.debug("Requesting GET /tags # {}",i);
            String existingTags = At.tags().get(String.class);
            log.debug("Tags: {}", existingTags);
            log.debug("Requesting GET /selections # {}",i);
            String existingSelections = At.selections().get(String.class);
            log.debug("Selections: {}", existingSelections);
        }
        long end = System.currentTimeMillis();
        log.debug("Jetty server time: {} ms", end-start);
        jetty.stop();
        
    }

}
