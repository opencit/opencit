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
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.mtwilson.model.*;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.My;
import com.intel.mtwilson.saml.TrustAssertion.HostTrustAssertion;
import java.io.File;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class JavaQuickstartExamples {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void register() throws Exception {
        File directory = new File(System.getProperty("user.home", "."));
        String username = "myusername"; // you choose a username
        String password = "changeit"; // you choose a password
        URL server = new URL("http://127.0.0.1:8080/mtwilson/v1");
        String[] roles = new String[] { "Attestation", "Whitelist" };
        KeystoreUtil.createUserInDirectory(directory, username, password, server, roles);
    }
    
    /*
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
    }*/
    
    @Test
    public void testSaml() throws Exception {
        File directory = new File(System.getProperty("user.home", "."));
        String username = "myusername"; // you choose a username
        String password = "changeit"; // you choose a password
        URL server = new URL("https://10.1.71.230:8443"); // your Mt Wilson server
        ApiClient api = KeystoreUtil.clientForUserInDirectory(directory, username, password, server);
        String saml = api.getSamlForHost(new Hostname("10.1.70.142"));
        TrustAssertion trust = api.verifyTrustAssertion(saml);
        assertNotNull(trust);
        Set<String> hostnames = trust.getHosts();
        for(String hostname : hostnames) {
            HostTrustAssertion hostTrustAssertion = trust.getTrustAssertion(hostname);
        log.debug("SAML Issuer: {}", hostTrustAssertion.getIssuer());
        log.debug("SAML Issued On: {}", hostTrustAssertion.getDate().toString());
        log.debug("SAML Subject: {}", hostTrustAssertion.getSubject());
        for(String attr : hostTrustAssertion.getAttributeNames()) {
            log.debug("Host {}: {}", attr, hostTrustAssertion.getStringAttribute(attr));
        }
        assertNull(hostTrustAssertion.getAikCertificate());
        log.debug("AIK Certificate: {}", hostTrustAssertion.getAikCertificate() == null ? "null" : Base64.encodeBase64String(hostTrustAssertion.getAikCertificate().getEncoded()));
        }
    }
}
