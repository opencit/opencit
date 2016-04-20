/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.glassfish.jersey.client.HttpUrlConnector;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyAwareConnectionFactory implements HttpUrlConnector.ConnectionFactory{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyAwareConnectionFactory.class);
    private TlsPolicy tlsPolicy;
    
    public TlsPolicyAwareConnectionFactory(TlsPolicy tlsPolicy) {
        this.tlsPolicy = tlsPolicy;
    }
    
    @Override
    public HttpURLConnection getConnection(URL url) throws IOException {
        log.debug("TlsPolicyAwareConnectionFactory getConnection: {}", url.toExternalForm());
        TlsConnection tlsConnection = new TlsConnection(url, tlsPolicy);
        return tlsConnection.openConnection();
    }
    
}
