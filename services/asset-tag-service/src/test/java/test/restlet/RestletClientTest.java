/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.restlet;

import java.io.IOException;
import org.junit.Test;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class RestletClientTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testGetIntranetWebpageOneLine() throws IOException {
        new ClientResource("http://cloudsecurityportal.intel.com").get().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }
    
    @Test
    public void testGetInternetWebpageWithProxy() throws IOException {
        // see also http://restlet.org/learn/javadocs/2.1/jee/engine/org/restlet/engine/connector/ClientConnectionHelper.html
        System.setProperty("http.proxyHost", "proxy-us.intel.com"); 
        System.setProperty("http.proxyPort", "911");
        ClientResource resource = new ClientResource("http://restlet.org");
        resource.get().write(System.out); // from tutorial http://restlet.org/learn/tutorial/2.1/
    }
    
}
