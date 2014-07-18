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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyAwareConnectionFactory.class);
    private TlsConnection tlsConnection;
    public TlsPolicyAwareConnectionFactory(TlsConnection tlsConnection) {
        this.tlsConnection = tlsConnection;
    }
    @Override
    public HttpURLConnection getConnection(URL url) throws IOException {
        log.debug("getConnection: {}", url.toExternalForm());
        return tlsConnection.openConnection();
    }
    
}
