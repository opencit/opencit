/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.http.tls;

import com.intel.mtwilson.crypto.RsaCredentialX509;
import com.intel.mtwilson.crypto.RsaUtil;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.crypto.X509Builder;
import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.io.ByteArrayResource;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
/**
 *
 * @author jbuhacoff
 */
public class TestTlsPolicyWithServerCaSigned {
    private static String caKeystorePath;
    private static Server jetty;
    
    private static void createTestCa(String filename) throws NoSuchAlgorithmException, CertificateEncodingException, KeyManagementException, KeyStoreException, IOException, CertificateException {
        // create ca cert
        KeyPair caKeys = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate caCert = X509Builder.factory().selfSigned("CN=testca", caKeys).expires(30, TimeUnit.DAYS).keyUsageCertificateAuthority().build();
        RsaCredentialX509 ca = new RsaCredentialX509(caKeys.getPrivate(), caCert);
        // create tls cert
        KeyPair tlsKeys = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate tlsCert = X509Builder.factory().subjectName("CN=testserver").subjectPublicKey(tlsKeys.getPublic()).issuerName(caCert).issuerPrivateKey(caKeys.getPrivate()).expires(30, TimeUnit.DAYS).keyUsageDataEncipherment().build();
        RsaCredentialX509 tls = new RsaCredentialX509(tlsKeys.getPrivate(), tlsCert);
        // save both into a keystore
        SimpleKeystore keystore = new SimpleKeystore(new File(filename), "password");
        keystore.addKeyPairX509(caKeys.getPrivate(), caCert, "ca", "password");
        keystore.addKeyPairX509(tlsKeys.getPrivate(), tlsCert, "server", "password");
        keystore.save();
    }
    
    @BeforeClass
    public static void startJetty() {
        // create a CA signed cert for the server
        caKeystorePath = System.getProperty("java.io.tmpdir")+File.separator+"ca.jks";
        createTestCa(caKeystorePath);
        
        int port = 17443;
        jetty = new Server(port);
        ContextHandlerCollection collection = new ContextHandlerCollection();
        jetty.setHandler(collection);
        SslContextFactory sslContextFactory = new SslContextFactory("server_keystore.jks");
    }
    
    @Test
    public void testKeystoreCertificateRepositoryLookupByAddress() throws Exception {
        // prepare a keystore with a single certificate
        KeyPair rsa = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate x509 = RsaUtil.generateX509Certificate("test1", rsa, 30);
        ByteArrayResource resource = new ByteArrayResource();
        SimpleKeystore keystore = new SimpleKeystore(resource, "password");
        keystore.addTrustedSslCertificate(x509, "test1");
        for(String alias : keystore.aliases()) { System.out.println("alias: "+alias); }
        // prepare the repository object
        KeystoreCertificateRepository repo = new KeystoreCertificateRepository(keystore);
        // lookup by address can return an exact match on alias, but not a partial match
        assertNotNull(repo.getCertificateForAddress(new InternetAddress("test1")));
        assertNull(repo.getCertificateForAddress(new InternetAddress("test1-1")));
        assertNull(repo.getCertificateForAddress(new InternetAddress("test")));
        assertNull(repo.getCertificateForAddress(new InternetAddress("est1")));
        // lookup by subject and issuer
        assertNotNull(repo.getCertificateForSubjectByIssuer("CN=test1,OU=Mt Wilson,O=Trusted Data Center,C=US", "CN=test1,OU=Mt Wilson,O=Trusted Data Center,C=US"));
        assertNull(repo.getCertificateForSubjectByIssuer("CN=test1,OU=Mt Wilson,O=Trusted Data Center,C=US", "CN=testXX,OU=Mt Wilson,O=Trusted Data Center,C=US"));
    }
}
