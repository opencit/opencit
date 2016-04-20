/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.mtwilson.api.ClientFactorySpi;
import com.intel.mtwilson.api.MtWilson;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import java.io.File;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java Service Provider Implementation for com.intel.mtwilson.api.ClientFactory defined in the
 * mtwilson-api module.
 * 
 * You can also use this class directly, or continue to use KeystoreUtil directly.
 * 
 * @author jbuhacoff
 */
public class ApiClientFactory implements ClientFactorySpi {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public SimpleKeystore createUserInResource(Resource keystore, String keystoreUsername, String keystorePassword, URL wsUrl, TlsPolicy tlsPolicy, String[] roles) {
        try {
            return KeystoreUtil.createUserInResource(keystore, keystoreUsername, keystorePassword, wsUrl, tlsPolicy, roles);
        }
        catch(Exception e) {
            log.error("Cannot create user in resource: "+e.toString(), e);
            return null;
        }
    }

    @Override
    public MtWilson clientForUserInResource(Resource keystore, String keystoreUsername, String keystorePassword, URL wsUrl, TlsPolicy tlsPolicy) {
        try {
            return KeystoreUtil.clientForUserInResource(keystore, keystoreUsername, keystorePassword, wsUrl, tlsPolicy);
        }
        catch(Exception e) {
            log.error("Cannot get client for user in resource: "+e.toString(), e);
            return null;
        }
    }
    
}
