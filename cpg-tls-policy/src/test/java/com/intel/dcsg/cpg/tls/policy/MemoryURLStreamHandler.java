/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.io.Resource;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jbuhacoff
 */
public class MemoryURLStreamHandler extends URLStreamHandler {
    public static final ConcurrentHashMap<String,Resource> map = new ConcurrentHashMap<String,Resource>();
    
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new ResourceURLConnection(url, map);
    }
 
    public static void register() {
        String handlers = System.getProperty("java.protocol.handler.pkgs");
        if( handlers == null || handlers.isEmpty() ) {
            handlers = MemoryURLStreamHandler.class.getName();
        }
        else {
            handlers = handlers.concat(",").concat(MemoryURLStreamHandler.class.getName());
        }
        System.setProperty("java.protocol.handler.pkgs", handlers);
    }
}
