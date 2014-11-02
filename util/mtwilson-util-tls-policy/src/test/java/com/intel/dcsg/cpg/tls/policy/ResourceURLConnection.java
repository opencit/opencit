/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.io.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 *
 * @author jbuhacoff
 */
public class ResourceURLConnection extends URLConnection {
    private Map<String,Resource> map;
    private Resource resource;
    
    public ResourceURLConnection(URL url, Map<String,Resource> map) {
        super(url);
        this.map = map;
    }

    @Override
    public void connect() throws IOException {
        if( connected ) { return; }
        resource = map.get(url.toString());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return resource.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return resource.getOutputStream();
    }

}
