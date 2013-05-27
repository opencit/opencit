/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package api.quickstart;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.ApiClientFactory;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.TrustAssertion;
import com.intel.mtwilson.api.MtWilson;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.tls.InsecureTlsPolicy;
import com.intel.mtwilson.tls.TlsPolicy;
import java.io.File;
import java.net.URL;
import java.security.cert.X509Certificate;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class JavaExamples {
    
    @Test
    public void register() throws Exception {
        File directory = new File(System.getProperty("user.home", "."));
        String username = "test2"; // you choose a username
        String password = "changeit"; // you choose a password
        URL server = new URL("https://10.1.71.80:8181"); // your Mt Wilson server
        String[] roles = new String[] { "Attestation", "Whitelist" };
        KeystoreUtil.createUserInDirectory(directory, username, password, server, roles);
    }
    
    @Test
    public void registerV2() throws Exception {
        ByteArrayResource keystoreResource = new ByteArrayResource();
        String keystoreUsername = "jonathan";
        String keystorePassword = "password";
        URL wsUrl = new URL("https://10.1.71.88:8181");
        TlsPolicy tlsPolicy = new InsecureTlsPolicy();
        String[] roles = new String[] { "Attestation", "Whitelist" };        
        ApiClientFactory factory = new ApiClientFactory();
        factory.createUserInResource(keystoreResource, keystoreUsername, keystorePassword, wsUrl, tlsPolicy, roles);
        MtWilson client = factory.clientForUserInResource(keystoreResource, keystoreUsername, keystorePassword, wsUrl, tlsPolicy);
        X509Certificate samlCertificate = client.getSamlCertificate();
        System.out.println("Mt Wilson SAML Certificate: "+samlCertificate.getSubjectX500Principal().getName());
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
        //X509Certificate aik = 
        trust.getAikCertificate();
        //Date issued = 
        trust.getDate();
        //String issuer = 
        trust.getIssuer();
        //String hostname =
        trust.getSubject();
        for(String attr : trust.getAttributeNames()) {
            //String signedAttribute = 
            trust.getStringAttribute(attr);
        }
    }
}
