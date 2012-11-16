/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package api.quickstart;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.TrustAssertion;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.datatypes.Hostname;
import java.io.File;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class JavaExamples {
    
    @Test
    public void register() throws Exception {
        File directory = new File(System.getProperty("user.home", "."));
        String username = "test1"; // you choose a username
        String password = "changeit"; // you choose a password
        URL server = new URL("https://10.1.71.212:8181"); // your Mt Wilson server
        String[] roles = new String[] { "Attestation", "Whitelist" };
        KeystoreUtil.createUserInDirectory(directory, username, password, server, roles);
    }
    
    @Test
    public void saml() throws Exception {
        File directory = new File(System.getProperty("user.home", "."));
        String username = "test1"; // you choose a username
        String password = "changeit"; // you choose a password
        URL server = new URL("https://10.1.71.212:8181"); // your Mt Wilson server
        ApiClient api = KeystoreUtil.clientForUserInDirectory(directory, username, password, server);
        String saml = api.getSamlForHost(new Hostname("192.168.1.100"));
        TrustAssertion trust = api.verifyTrustAssertion(saml);
        X509Certificate aik = trust.getAikCertificate();
        Date issued = trust.getDate();
        String issuer = trust.getIssuer();
        String hostname = trust.getSubject();
        for(String attr : trust.getAttributeNames()) {
            String signedAttribute = trust.getStringAttribute(attr);
        }
    }
}
