/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.My;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.model.*;
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
    
    /**
     * Connecting to Glassfish 3.1 with SSLv2:  cannot initialize client
     * Connecting to Glassfish 3.1 with SSL, SSLv3, TLS, TLSv1.1, TLSv1.2:  javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
     * 
     * @throws ClientException
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Test
    public void testTrustCaAndVerifyHostname() throws ClientException, IOException, ApiException, SignatureException {
        Properties config = new Properties();
        config.setProperty("mtwilson.api.baseurl", "https://localhost:8181/");
        config.setProperty("mtwilson.api.keystore", My.configuration().getKeystoreFile().getAbsolutePath()); // was: System.getProperty("user.home")+File.separator+"test2.jks");
        config.setProperty("mtwilson.api.keystore.password", My.configuration().getKeystorePassword()); // was: "changeit"
        config.setProperty("mtwilson.api.key.alias", My.configuration().getConfiguration().getString("mtwilson.api.key.alias")); // was:  "test2"
        config.setProperty("mtwilson.api.key.password", My.configuration().getKeystorePassword()); // was: "changeit"  ... usng same password as keystore for this
        config.setProperty("mtwilson.api.ssl.protocol", "TLSv1.1");
//        config.setProperty("mtwilson.api.ssl.verifyHostname", "true");
//        config.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
        config.setProperty("mtwilson.api.ssl.policy", "TRUST_KNOWN_CERTIFICATE");
        
        ApiClient c = new ApiClient(new MapConfiguration(config));
        //String saml =
        c.getSamlForHost(new Hostname("1.2.3.4")); // IOException, ApiException, SignatureException
    }
}
