/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client.jaxrs;

import java.util.Map;
import java.util.Properties;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.client.ClientConfig;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import org.glassfish.jersey.filter.LoggingFilter;
/**
 *
 * @author jbuhacoff
 */
public class MtWilsonClient extends JaxrsClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MtWilsonClient.class);
    
    public MtWilsonClient(JaxrsClient client) {
        super(client);
    }
    
    public MtWilsonClient(URL url) throws Exception {
        super(JaxrsClientBuilder.factory().url(url).build());
    }

    public MtWilsonClient(Properties properties) throws Exception {
        super(JaxrsClientBuilder.factory().configuration(properties).build());
    }
    public MtWilsonClient(Configuration configuration) throws Exception {
        super(JaxrsClientBuilder.factory().configuration(configuration).build());
    }
    
    public MtWilsonClient(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(JaxrsClientBuilder.factory().configuration(properties).tlsConnection(tlsConnection).build());
    }

}
