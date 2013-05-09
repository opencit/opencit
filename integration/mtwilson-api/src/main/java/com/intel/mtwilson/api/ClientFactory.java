/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.api;

import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.io.FileResource;
import com.intel.mtwilson.io.Resource;
import java.io.File;
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
    // XXX TODO  add TlsPolicy as a second parameter... after we transition to using cpg-tls-policy with the new factory classes and repositories
    public static SimpleKeystore createUserInResource(Resource keystore, String username, String password, URL webserviceUrl, String[] roles) {
        Iterator<ClientFactorySpi> factories = ServiceLoader.load(ClientFactorySpi.class).iterator();
        while(factories.hasNext()) {
            try {
                ClientFactorySpi factory = factories.next();
                SimpleKeystore client = factory.createUserInResource(keystore, username, password, webserviceUrl, roles);
                if( client != null ) {
                    return client;
                }
            }
            catch(ServiceConfigurationError e) {
                log.error(e.toString());
            }
        }
        return null;
    }
    public static MtWilson clientForUserInResource(Resource keystore, String username, String password, URL webserviceUrl) {
        Iterator<ClientFactorySpi> factories = ServiceLoader.load(ClientFactorySpi.class).iterator();
        while(factories.hasNext()) {
            try {
                ClientFactorySpi factory = factories.next();
                MtWilson client = factory.clientForUserInResource(keystore, username, password, webserviceUrl);
                if( client != null ) {
                    return client;
                }
            }
            catch(ServiceConfigurationError e) {
                log.error(e.toString());
            }
        }
        return null;
    }
    
}
