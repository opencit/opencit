/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.restlet;

import com.intel.mtwilson.atag.resource.TagResource;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TagApiTest1 {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static Server server;
    private static int port = 17222;
    private static String baseurl() { return String.format("http://127.0.0.1:%d", port); }
    
    @BeforeClass
    public static void startServer() throws Exception {
        server = new Server(Protocol.HTTP, port, TagResource.class);
        server.start();
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        server.stop();
    }
    
    @Test
    public void testHelloWorld() throws IOException {
//        new ClientResource("http://localhost:8182").get().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
        new ClientResource(baseurl()).get().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }
    
    @Test
    public void testDeleteHelloWorld() throws IOException {
//        new ClientResource("http://localhost:8182").get().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
        new ClientResource(baseurl()).delete().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }

    @Test
    public void testPutHelloWorld() throws IOException {
//        new ClientResource("http://localhost:8182").get().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
        new ClientResource(baseurl()).put("my update", MediaType.TEXT_PLAIN).write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }
    
}
