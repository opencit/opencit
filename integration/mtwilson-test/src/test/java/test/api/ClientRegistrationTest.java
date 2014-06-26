/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.api;

import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.My;
import com.intel.mtwilson.test.RemoteIntegrationTest;
import java.io.File;
import java.net.URL;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ClientRegistrationTest extends RemoteIntegrationTest {
    
    /**
     * Register using V1 APIs. Before running this junit test, create the 
     * file C:\mtwilson\configuration\mtwilson.properties with content like
     * this:
     * 
mtwilson.api.username=jonathan
mtwilson.api.password=password
mtwilson.api.url=http\://10.1.71.134\:8080/mtwilson/v1
mtwilson.api.baseurl=http\://10.1.71.134\:8080/mtwilson/v1
mtwilson.default.tls.policy.name=TRUST_FIRST_CERTIFICATE
mtwilson.api.keystore=c\:/mtwilson/configuration/jonathan.jks
mtwilson.api.keystore.password=beXyfVzb5D8oSHucNErVyw\=\=
mtwilson.api.key.alias=CN\=jonathan
mtwilson.api.key.password=beXyfVzb5D8oSHucNErVyw\=\=
     * 
     * The resulting client keystore can then be used with the junit tests
     * in the ApiTest class.
     * 
     * Note that the mtwilson.api.keystore is the complete file path with .jks extension,
     * and that the property used for the password is mtwilson.api.keystore.password.
     * 
     * That's important because if you use mtwilson.api.username and mtwilson.api.password
     * only in the test.properties file, the client will use HTTP BASIC authentication.
     * But if you put BOTH then it will use X509 because it has higher priority.
     * 
     * So using code below, you would set mtwilson.api.keystore to something like (configuration path)/(value of mtwilson.api.username).jks
     * 
     * @throws Exception 
     */
    @Test
    public void registerWithConfiguration() throws Exception {
        File directory = new File(My.filesystem().getConfigurationPath());
        String username = testProperties.getProperty("mtwilson.api.username");
        String password = testProperties.getProperty("mtwilson.api.keystore.password");
        URL server = new URL(testProperties.getProperty("mtwilson.api.url")); // My.configuration().getMtWilsonURL();
        String[] roles = new String[] { "Attestation", "Whitelist" };
        KeystoreUtil.createUserInDirectory(directory, username, password, server, roles);
    }
    
    
}
