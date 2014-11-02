/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.tls.policy.impl.*;
import java.io.File;
import java.util.Properties;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyFactoryTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    public static class MtWilsonTlsPolicyFactory {
        // property keys
        public static String TLS_POLICY_NAME = "tls.policy.name";
        public static String TLS_POLICY_KEYSTORE_FILE = "tls.policy.keystore.file";
        public static String TLS_POLICY_KEYSTORE_PASSWORD = "tls.policy.keystore.password";
        // policy names
        public static String INSECURE = "INSECURE";
        public static String TRUST_FIRST_CERTIFICATE = "TRUST_FIRST_CERTIFICATE";
        public static String TRUST_KNOWN_CERTIFICATE = "TRUST_KNOWN_CERTIFICATE";
        public static String TRUST_CA_VERIFY_HOSTNAME = "TRUST_CA_VERIFY_HOSTNAME"; // same as strict
        public static String STRICT = "STRICT";
        private String getRequiredKey(String keyName, Properties p) {
            if( !p.containsKey(keyName) ) { throw new IllegalArgumentException("Missing configuration key: "+keyName); }
            String value = p.getProperty(keyName);
            if( value.isEmpty() ) { throw new IllegalArgumentException("Invalid configuration value: "+keyName); }
            return value;
        }
        public TlsPolicy create(Properties p) {
            // tls policy name is required in order to create a policy; all other settings depend on the chosen policy
            String tlsPolicyName = getRequiredKey(TLS_POLICY_NAME, p);
            if( tlsPolicyName.equals(INSECURE) ) {
                return new InsecureTlsPolicy();
            }
            if( tlsPolicyName.equals(TRUST_FIRST_CERTIFICATE) ) {
                // requires a repository location;  for now assume it's a file.  XXX TODO allow also a resource specification which the application could provide to the factory like a jdbc reader etc.
                String tlsPolicyKeystoreFile = getRequiredKey(TLS_POLICY_KEYSTORE_FILE, p);
                String tlsPolicyKeystorePassword = getRequiredKey(TLS_POLICY_KEYSTORE_PASSWORD, p);
                try {
                    return TlsPolicyBuilder.factory().insecureTrustFirstCertificateWithKeystore(new File(tlsPolicyKeystoreFile), tlsPolicyKeystorePassword).build();
                }
                catch(Exception e) {
                    throw new IllegalArgumentException("Cannot load certificate repository: "+e.toString(), e);
                }
            }
            if( tlsPolicyName.equals(TRUST_KNOWN_CERTIFICATE) ) {
                // requires a repository location;  for now assume it's a file.  XXX TODO allow also a resource specification which the application could provide to the factory like a jdbc reader etc.
                String tlsPolicyKeystoreFile = getRequiredKey(TLS_POLICY_KEYSTORE_FILE, p);
                String tlsPolicyKeystorePassword = getRequiredKey(TLS_POLICY_KEYSTORE_PASSWORD, p);
                try {
                    return TlsPolicyBuilder.factory().strictWithKeystore(tlsPolicyKeystoreFile, tlsPolicyKeystorePassword).skipHostnameVerification().build();
                }
                catch(Exception e) {
                    throw new IllegalArgumentException("Cannot load certificate repository: "+e.toString(), e);
                }
            }
            if( tlsPolicyName.equals(STRICT) || tlsPolicyName.equals(TRUST_CA_VERIFY_HOSTNAME) ) {
                // requires a repository location;  for now assume it's a file.  XXX TODO allow also a resource specification which the application could provide to the factory like a jdbc reader etc.
                String tlsPolicyKeystoreFile = getRequiredKey(TLS_POLICY_KEYSTORE_FILE, p);
                String tlsPolicyKeystorePassword = getRequiredKey(TLS_POLICY_KEYSTORE_PASSWORD, p);
                try {
                    return TlsPolicyBuilder.factory().strictWithKeystore(tlsPolicyKeystoreFile, tlsPolicyKeystorePassword).build();
                }
                catch(Exception e) {
                    throw new IllegalArgumentException("Cannot load certificate repository: "+e.toString(), e);
                }
            }
            throw new IllegalArgumentException("Unknown policy: "+tlsPolicyName);
        }
    }
    
    @Test
    public void testCreateInsecureTlsPolicy() {
        Properties p = new Properties();
        p.setProperty("tls.policy.name", "INSECURE");
        MtWilsonTlsPolicyFactory factory = new MtWilsonTlsPolicyFactory();
        TlsPolicy tlsPolicy = factory.create(p);
        assertEquals(InsecureTlsPolicy.class.getName(), tlsPolicy.getClass().getName());
    }
    
    /*
    @Test
    public void testCreateTrustFirstCertificateTlsPolicy() {
        Properties p = new Properties();
        p.setProperty("tls.policy.name", "TRUST_FIRST_CERTIFICATE");
        p.setProperty("tls.policy.keystore.file", System.getProperty("user.home", ".")+File.separator+"test.jks");
        p.setProperty("tls.policy.keystore.password", "changeit");
        MtWilsonTlsPolicyFactory factory = new MtWilsonTlsPolicyFactory();
        TlsPolicy tlsPolicy = factory.create(p);
        assertEquals(TrustKnownCertificateTlsPolicy.class.getName(), tlsPolicy.getClass().getName());
        assertEquals(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER.getClass().getName(),tlsPolicy.getHostnameVerifier().getClass().getName());
        assertEquals(TrustKnownCertificateTlsPolicy.class.getName(), tlsPolicy.getTrustManager().getClass().getName());
    }

    @Test
    public void testCreateTrustKnownCertificateTlsPolicy() {
        Properties p = new Properties();
        p.setProperty("tls.policy.name", "TRUST_KNOWN_CERTIFICATE");
        p.setProperty("tls.policy.keystore.file", System.getProperty("user.home", ".")+File.separator+"test.jks");
        p.setProperty("tls.policy.keystore.password", "changeit");
        MtWilsonTlsPolicyFactory factory = new MtWilsonTlsPolicyFactory();
        TlsPolicy tlsPolicy = factory.create(p);
        assertEquals(TrustKnownCertificateTlsPolicy.class.getName(), tlsPolicy.getClass().getName());
        assertEquals(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER.getClass().getName(),tlsPolicy.getHostnameVerifier().getClass().getName());
        assertEquals(TrustKnownCertificateTlsPolicy.class.getName(), tlsPolicy.getTrustManager().getClass().getName());
    }
    
    @Test
    public void testCreateStrictTlsPolicy() {
        Properties p = new Properties();
        p.setProperty("tls.policy.name", "STRICT");
        p.setProperty("tls.policy.keystore.file", System.getProperty("user.home", ".")+File.separator+"test.jks");
        p.setProperty("tls.policy.keystore.password", "changeit");
        MtWilsonTlsPolicyFactory factory = new MtWilsonTlsPolicyFactory();
        TlsPolicy tlsPolicy = factory.create(p);
        assertEquals(StrictTlsPolicy.class.getName(), tlsPolicy.getClass().getName());
        assertEquals(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER.getClass().getName(),tlsPolicy.getHostnameVerifier().getClass().getName());
        assertEquals(StrictTlsPolicy.class.getName(), tlsPolicy.getTrustManager().getClass().getName());
    }
    */
}
