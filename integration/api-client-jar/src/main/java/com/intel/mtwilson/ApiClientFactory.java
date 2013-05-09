/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.mtwilson.api.ClientFactorySpi;
import com.intel.mtwilson.api.MtWilson;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.io.Resource;
import java.io.File;
import java.net.URL;

/**
 * Java Service Provider Implementation for com.intel.mtwilson.api.ClientFactory defined in the
 * mtwilson-api module.
 * 
 * You can also use this class directly, or continue to use KeystoreUtil directly.
 * 
 * @author jbuhacoff
 */
public class ApiClientFactory implements ClientFactorySpi {

    @Override
    public SimpleKeystore createUserInResource(Resource keystore, String keystoreUsername, String keystorePassword, URL wsUrl, String[] roles) {
        try {
            return KeystoreUtil.createUserInResource(keystore, keystoreUsername, keystorePassword, wsUrl, roles);
        }
        catch(Exception e) {
            return null;
        }
    }

    @Override
    public MtWilson clientForUserInResource(Resource keystore, String keystoreUsername, String keystorePassword, URL wsUrl) {
        try {
            return KeystoreUtil.clientForUserInResource(keystore, keystoreUsername, keystorePassword, wsUrl);
        }
        catch(Exception e) {
            return null;
        }
    }
    
}
