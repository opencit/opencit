/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.java.net;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class URLTest {
    @Test
    public void testUsernameAndPasswordInURL() throws MalformedURLException {
        URL loginURL = new URL("https://username:password@1.2.3.4/path");
        System.out.println("protocol: "+loginURL.getProtocol());
        System.out.println("authority: "+loginURL.getAuthority());
        System.out.println("userinfo: "+loginURL.getUserInfo());
        System.out.println("host: "+loginURL.getHost());
        System.out.println("port: "+loginURL.getPort());
        System.out.println("default port: "+loginURL.getDefaultPort());
        System.out.println("path: "+loginURL.getPath());
    }
}
