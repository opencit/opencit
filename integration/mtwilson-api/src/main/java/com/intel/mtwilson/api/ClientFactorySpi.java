/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.api;

import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.io.Resource;
import com.intel.mtwilson.tls.TlsPolicy;
import java.net.URL;
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
public interface ClientFactorySpi {
//    MtWilson createClientFor(URL webserviceUrl); // XXX TODO  add TlsPolicy as a second parameter... after we transition to using cpg-tls-policy with the new factory classes and repositories

    SimpleKeystore createUserInResource(Resource keystoreDir, String keystoreUsername, String keystorePassword, URL wsUrl, TlsPolicy tlsPolicy, String[] roles);
    MtWilson clientForUserInResource(Resource keystoreDir, String keystoreUsername, String keystorePassword, URL wsUrl, TlsPolicy tlsPolicy);

}
