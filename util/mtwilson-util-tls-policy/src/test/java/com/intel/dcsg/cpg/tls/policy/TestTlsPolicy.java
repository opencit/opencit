/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.x509.repository.KeystoreCertificateRepository;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.junit.AfterClass;
import com.intel.dcsg.cpg.crypto.KeyStoreUtil;

/**
 *
 * @author jbuhacoff
 */
public class TestTlsPolicy {
    private static Server jetty;
    
    @BeforeClass
    public static void startJetty() throws Exception {
        int port = 17443;
        jetty = new Server(port);
        jetty.start();
        jetty.setHandler(new HelloWorldHandler());
//        SslContextFactory sslContextFactory = new SslContextFactory("server_keystore.jks");
    }
    
    @AfterClass
    public static void stopJetty() throws Exception {
        jetty.stop();
        jetty.join();
    }

    @Test
    public void testKeystoreCertificateRepositoryLookupByAddress() throws Exception {
        // prepare a keystore with a single certificate
        String keystorePassword = "password";
        KeyPair rsa = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate x509 = RsaUtil.generateX509Certificate("test1", rsa, 30);
        KeyStore keystore = KeyStoreUtil.createWithPassword(keystorePassword);
        keystore.setCertificateEntry("sslcert", x509);
        // prepare the repository object
        KeystoreCertificateRepository repo = new KeystoreCertificateRepository(keystore, keystorePassword);
    }
}
