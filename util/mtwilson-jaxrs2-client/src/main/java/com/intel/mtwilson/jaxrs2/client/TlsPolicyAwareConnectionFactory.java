/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.glassfish.jersey.client.HttpUrlConnector.ConnectionFactory;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyAwareConnectionFactory implements ConnectionFactory {
    private TlsConnection tlsConnection;
    public TlsPolicyAwareConnectionFactory(TlsConnection tlsConnection) {
        this.tlsConnection = tlsConnection;
    }
    @Override
    public HttpURLConnection getConnection(URL url) throws IOException {
        return tlsConnection.openConnection();
    }
    
}
