/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.ApiException;
import com.intel.mtwilson.ClientException;
import com.intel.mtwilson.datatypes.Hostname;
import java.io.File;
import java.io.IOException;
import java.security.SignatureException;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyTest {
    @Test
    public void testTrustCaAndVerifyHostname() throws ClientException, IOException, ApiException, SignatureException {
        Properties config = new Properties();
        config.setProperty("mtwilson.api.baseurl", "https://10.1.71.80:8181");
        config.setProperty("mtwilson.api.keystore", System.getProperty("user.home")+File.separator+"test2.jks");
        config.setProperty("mtwilson.api.keystore.password", "changeit");
        config.setProperty("mtwilson.api.key.alias", "test2");
        config.setProperty("mtwilson.api.key.password", "changeit");
//        config.setProperty("mtwilson.api.ssl.verifyHostname", "true");
//        config.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
        config.setProperty("mtwilson.api.ssl.policy", "TRUST_KNOWN_CERTIFICATE");
        
        ApiClient c = new ApiClient(new MapConfiguration(config));
        //String saml =
        c.getSamlForHost(new Hostname("1.2.3.4")); // IOException, ApiException, SignatureException
    }
}
