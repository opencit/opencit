/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package example.restlet;

import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One difference between Component and Server is that Component automatically writes INFO logs for incoming
 * requests (like access log)
 * @author jbuhacoff
 */
public class ObjectBoxTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static Component component;
    private static int port = 17222;
    private static String baseurl() { return String.format("http://127.0.0.1:%d/", port); }
    
    public static class BoxApplication extends Application {
        @Override
        public synchronized Restlet createInboundRoot() {
            Router router = new Router(getContext());
            router.attach("boxes", ObjectBoxListResource.class);
            router.attach("boxes/{id}", ObjectBoxResource.class);
            return router;
        }
    }
    
    @BeforeClass
    public static void startServer() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, port);
        component.getDefaultHost().attach("/", new BoxApplication()); // if the restlet attaches to "/fruit", this must be "", not "/";  but if the restlet attaches to "fruit", then this can be "/"
        component.start();
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        component.stop();
    }
    
    @Test
    public void testHelloWorldBox() throws IOException {
        new ClientResource(baseurl()+"boxes").get().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
        new ClientResource(baseurl()+"boxes/123").get().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }
    
    @Test
    public void testDeleteHelloWorldBox() throws IOException {
        new ClientResource(baseurl()+"boxes/123").delete().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }
    
    @Test
    public void testPutHelloWorldBoxJson() throws IOException {
        new ClientResource(baseurl()+"boxes/123").put(new ObjectBox("D", new Fruit[] {}), MediaType.APPLICATION_JSON).write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }

    @Test
    public void testPutHelloWorldBoxBinary() throws IOException {
//        new ClientResource(baseurl()+"boxes/123").put("testing binary data".getBytes(), MediaType.APPLICATION_OCTET_STREAM).write(System.out); //this causes the text "dGVzdGluZyBiaW5hcnkgZGF0YQ=="  (WITH quotes because it's json) to be sent as application/json  even though i specified octet stream! and sent bytes!!
        new ClientResource(baseurl()+"boxes/123").put(new ByteArrayRepresentation("testing binary data".getBytes()), MediaType.APPLICATION_OCTET_STREAM).write(System.out); //this causes the text "testing binary data" (without quotes) to be sent as application/octet-stream
    }
    
    @Test(expected=Exception.class)
    public void testPutHelloWorldBoxWrongMediaType() throws IOException {
        new ClientResource(baseurl()+"boxes/123").put("my update", MediaType.TEXT_PLAIN).write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }
    
    @Test
    public void testPostHelloWorldBox() throws IOException {
        new ClientResource(baseurl()+"boxes").post(new ObjectBox("C", new Fruit[] { new Fruit("banana", "yellow")}), MediaType.APPLICATION_JSON).write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }

    
}
