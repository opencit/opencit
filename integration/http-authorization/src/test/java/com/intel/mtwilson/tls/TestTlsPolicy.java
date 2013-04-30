/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls;

import com.intel.mtwilson.crypto.RsaUtil;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.model.InternetAddress;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author jbuhacoff
 */
public class TestTlsPolicy {/*
    private static Server jetty;
    
    @BeforeClass
    public static void startJetty() {
        int port = 17443;
        jetty = new Server(port);
        ContextHandlerCollection collection = new ContextHandlerCollection();
        jetty.setHandler(collection);
        SslContextFactory sslContextFactory = new SslContextFactory("server_keystore.jks");
    }*/
    
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
        assertNotNull(repo.getCertificateForSubject("CN=test1,OU=Mt Wilson,O=Trusted Data Center,C=US"));
        assertEquals(1, repo.getCertificateForSubject("CN=test1,OU=Mt Wilson,O=Trusted Data Center,C=US").size());
    }
}
