/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.api;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import java.net.URL;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * To register your factory implementation, create the file
 * META-INF/services/com.intel.mtwilson.api.ClientFactory and set its contents like this:
# My Client Factory Implementation:
com.intel.my.app.MyClientFactory
 * 
 *
 * @author jbuhacoff
 */
public class ClientFactory {
    private static final Logger log = LoggerFactory.getLogger(ClientFactory.class);

    public static SimpleKeystore createUserInResource(Resource keystore, String username, String password, URL webserviceUrl, TlsPolicy tlsPolicy, String[] roles) {
        Iterator<ClientFactorySpi> factories = ServiceLoader.load(ClientFactorySpi.class).iterator();
        while(factories.hasNext()) {
            try {
                ClientFactorySpi factory = factories.next();
                log.debug("ClientFactory trying implementation: "+factory.getClass().getName());
                SimpleKeystore client = factory.createUserInResource(keystore, username, password, webserviceUrl, tlsPolicy, roles);
                if( client != null ) {
                    return client;
                }
            }
            catch(ServiceConfigurationError e) {
                log.error(e.toString());
            }
        }
        log.error("No implementation available for: "+ClientFactorySpi.class.getName());
        return null;
    }
    
    public static MtWilson clientForUserInResource(Resource keystore, String username, String password, URL webserviceUrl, TlsPolicy tlsPolicy) {
        Iterator<ClientFactorySpi> factories = ServiceLoader.load(ClientFactorySpi.class).iterator();
        while(factories.hasNext()) {
            try {
                ClientFactorySpi factory = factories.next();
                MtWilson client = factory.clientForUserInResource(keystore, username, password, webserviceUrl, tlsPolicy);
                if( client != null ) {
                    return client;
                }
            }
            catch(ServiceConfigurationError e) {
                log.error(e.toString());
            }
        }
        log.error("No implementation available for: "+ClientFactorySpi.class.getName());
        return null;
    }
    
}
