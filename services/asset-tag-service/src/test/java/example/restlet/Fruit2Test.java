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
import org.restlet.resource.ClientResource;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One difference between Component and Server is that Component automatically writes INFO logs for incoming
 * requests (like access log)
 * @author jbuhacoff
 */
public class Fruit2Test {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static Server server;
    private static Component component;
    private static int port = 17222;
    private static String baseurl() { return String.format("http://127.0.0.1:%d/", port); }
    
    public static class FruitApplication extends Application {
        @Override
        public synchronized Restlet createInboundRoot() {
            Router router = new Router(getContext());
            router.attach("fruits", FruitListResource.class);
            router.attach("fruits/{id}", FruitResource.class);
            router.attach("seeds", SeedListResource.class);
            router.attach("seeds/{id}", SeedResource.class);
            return router;
        }
    }
    
    @BeforeClass
    public static void startServer() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, port);
        component.getDefaultHost().attach("/", new FruitApplication()); // if the restlet attaches to "/fruit", this must be "", not "/";  but if the restlet attaches to "fruit", then this can be "/"
        component.start();
//        server = new Server(Protocol.HTTP, port, new FruitApplication());
//        server.start();
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        component.stop();
//        server.stop();
    }
    
    @Test
    public void testHelloWorldFruit() throws IOException {
        new ClientResource(baseurl()+"fruits").get().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
        new ClientResource(baseurl()+"fruits/123").get().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }
    
    @Test
    public void testHelloWorldSeed() throws IOException {
        new ClientResource(baseurl()+"fruits").get().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
        new ClientResource(baseurl()+"fruits/123").get().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }

    @Test
    public void testDeleteHelloWorldFruit() throws IOException {
        new ClientResource(baseurl()+"fruits/123").delete().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }

    @Test
    public void testDeleteHelloWorldSeed() throws IOException {
        new ClientResource(baseurl()+"seeds/123").delete().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }
    
    @Test
    public void testPutHelloWorldFruit() throws IOException {
        new ClientResource(baseurl()+"fruits").put("my update", MediaType.TEXT_PLAIN).write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
        new ClientResource(baseurl()+"fruits/123").put("my update", MediaType.TEXT_PLAIN).write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
        new ClientResource(baseurl()+"fruits/123").put(new Fruit("orange", "orange"), MediaType.APPLICATION_JSON).write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }

    @Test
    public void testPutHelloWorldSeed() throws IOException {
        new ClientResource(baseurl()+"seeds").put("my update", MediaType.TEXT_PLAIN).write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
        new ClientResource(baseurl()+"seeds/123").put("my update", MediaType.TEXT_PLAIN).write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }
    
}
