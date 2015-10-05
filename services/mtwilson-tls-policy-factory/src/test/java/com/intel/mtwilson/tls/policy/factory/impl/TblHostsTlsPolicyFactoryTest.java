/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyManager;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.dcsg.cpg.tls.policy.TrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.DenyAllHostnameVerifier;
import com.intel.dcsg.cpg.tls.policy.impl.FirstCertificateTrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.FirstPublicKeyTrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyDigestTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.CertificateRepository;
import com.intel.dcsg.cpg.x509.repository.HashSetMutableCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.HashSetMutablePublicKeyRepository;
import com.intel.dcsg.cpg.x509.repository.MutableCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.MutablePublicKeyRepository;
import com.intel.dcsg.cpg.x509.repository.PublicKeyRepository;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTrustFirstPublicKeyTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.PublicKeyDigestTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import javax.net.ssl.X509ExtendedTrustManager;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

/**
 *
 * @author jbuhacoff
 */
public class TblHostsTlsPolicyFactoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TblHostsTlsPolicyFactoryTest.class);
    private ObjectMapper mapper = new ObjectMapper();
    
    @BeforeClass
    public static void registerExtensions() {
        Extensions.register(TlsPolicyCreator.class, InsecureTlsPolicyCreator.class); // required for testInsecureV1 and testInsecureV2
        Extensions.register(TlsPolicyCreator.class, PublicKeyDigestTlsPolicyCreator.class); // required for testPublicKeyDigestTlsPolicyV2
        Extensions.register(TlsPolicyCreator.class, InsecureTrustFirstPublicKeyTlsPolicyCreator.class); // required for testTrustFirstPublicKeyTlsPolicyV2
    }
    
    private TblHosts getHost() {
        TblHosts host = new TblHosts();
        host.setName("10.1.71.128");
        host.setPort(1443);
//        host.setName("10.1.71.162");
//        host.setPort(443);
        return host;
    }
    
    @Test
    public void testInsecureV1() {
        TblHosts host = getHost();
        host.setTlsPolicyName("INSECURE");
        TblHostsTlsPolicyFactory factory = new TblHostsTlsPolicyFactory(host);
        TlsPolicy tlsPolicy = factory.getTlsPolicy();
        assertEquals(InsecureTlsPolicy.class, tlsPolicy.getClass());
    }

    @Test
    public void testInsecureV2() {
        TblHosts host = getHost();
        TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
        tlsPolicyChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
        tlsPolicyChoice.getTlsPolicyDescriptor().setPolicyType("INSECURE");
        host.setTlsPolicyChoice(tlsPolicyChoice);
        TblHostsTlsPolicyFactory factory = new TblHostsTlsPolicyFactory(host);
        TlsPolicy tlsPolicy = factory.getTlsPolicy();
        assertEquals(InsecureTlsPolicy.class, tlsPolicy.getClass());
    }

    @Test
    public void testPublicKeyDigestTlsPolicyV2() {
        TblHosts host = getHost();
        TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
        tlsPolicyChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
        tlsPolicyChoice.getTlsPolicyDescriptor().setPolicyType("public-key-digest");
        tlsPolicyChoice.getTlsPolicyDescriptor().setData(new ArrayList<String>());
        tlsPolicyChoice.getTlsPolicyDescriptor().getData().add("18 9a e6 e0 26 6f ae 63 8f 8c 9c b0 92 e1 ad 04 c3 a7 58 ab");
        host.setTlsPolicyChoice(tlsPolicyChoice);
        TblHostsTlsPolicyFactory factory = new TblHostsTlsPolicyFactory(host);
        TlsPolicy tlsPolicy = factory.getTlsPolicy();
        assertEquals(PublicKeyDigestTlsPolicy.class, tlsPolicy.getClass());
    }

    @Test
    public void testCertificateDigestTlsPolicyV2() {
        TblHosts host = getHost();
        TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
        tlsPolicyChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
        tlsPolicyChoice.getTlsPolicyDescriptor().setPolicyType("certificate-digest");
        tlsPolicyChoice.getTlsPolicyDescriptor().setData(new ArrayList<String>());
        tlsPolicyChoice.getTlsPolicyDescriptor().getData().add("18 9a e6 e0 26 6f ae 63 8f 8c 9c b0 92 e1 ad 04 c3 a7 58 ab");
        host.setTlsPolicyChoice(tlsPolicyChoice);
        TblHostsTlsPolicyFactory factory = new TblHostsTlsPolicyFactory(host);
        TlsPolicy tlsPolicy = factory.getTlsPolicy();
        assertEquals(PublicKeyDigestTlsPolicy.class, tlsPolicy.getClass());
    }
    
    /**
     * Example output:
     * <pre>
     * initial policy: {"tlsPolicyId":null,"tlsPolicyDescriptor":{"policyType":"TRUST_FIRST_CERTIFICATE","ciphers":null,"protocols":null,"protection":null,"data":[],"meta":null}}
     * </pre>
     * 
     * @throws MalformedURLException
     * @throws IOException 
     */
    @Test
    public void testTrustFirstPublicKeyTlsPolicyV2() throws MalformedURLException, IOException {
        TblHosts host = getHost();
        TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
        tlsPolicyChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
        tlsPolicyChoice.getTlsPolicyDescriptor().setPolicyType("TRUST_FIRST_CERTIFICATE");
        tlsPolicyChoice.getTlsPolicyDescriptor().setData(new ArrayList<String>());
        log.debug("initial policy: {}", mapper.writeValueAsString(tlsPolicyChoice));
        host.setTlsPolicyChoice(tlsPolicyChoice);
        TblHostsTlsPolicyFactory factory = new TblHostsTlsPolicyFactory(host);
        TlsPolicy tlsPolicy = factory.getTlsPolicy();
        assertEquals(PublicKeyTlsPolicy.class, tlsPolicy.getClass());
        
        // test it
        TlsConnection tlsConnection = new TlsConnection(new URL(String.format("https://%s:%d",host.getName(), host.getPort())), tlsPolicy);
        HttpsURLConnection connection = tlsConnection.openConnection();
        try(InputStream in = connection.getInputStream()) {
            String content = IOUtils.toString(in);
            log.debug("content: {}", content);
        }
        catch(Exception e) {
            log.debug("error: {}", e.getMessage()); // for example we would typically get 401 unauthorized error from trust agent if we don't specify username and password -- but that's ok because we are testing the tls policy here 
        }
        connection.disconnect();
        log.debug("updated policy: {}", mapper.writeValueAsString(tlsPolicyChoice));
    }


    /**
     * Example output:
     * <pre>
     * initial policy: {"tlsPolicyId":null,"tlsPolicyDescriptor":{"policyType":"TRUST_FIRST_CERTIFICATE","ciphers":null,"protocols":null,"protection":null,"data":[],"meta":null}}
     * </pre>
     * 
     * @throws MalformedURLException
     * @throws IOException 
     */
    @Test
    public void testTrustFirstPublicKeyTlsPolicy() throws MalformedURLException, IOException, NoSuchAlgorithmException, KeyManagementException {
        TblHosts host = getHost();
        /*
        // abbreviation of what InsecureTrustFirstPublicKeyTlsPolicyCreator does:
        MutablePublicKeyRepository repository = new HashSetMutablePublicKeyRepository();
        TrustDelegate delegate = new FirstPublicKeyTrustDelegate(repository);
        TlsPolicy tlsPolicy = new PublicKeyTlsPolicy(repository, delegate);
        * */
        MutableCertificateRepository repository = new HashSetMutableCertificateRepository();
        TrustDelegate delegate = new FirstCertificateTrustDelegate(repository);
        TlsPolicy tlsPolicy = new CertificateTlsPolicy(repository, delegate);
        
        // repository starts empty
        printRepository(repository);
        
        // test it
//        TlsConnection tlsConnection = new TlsConnection(new URL(String.format("https://%s:%d",host.getName(), host.getPort())), tlsPolicy);
//        HttpsURLConnection connection = tlsConnection.openConnection();

        // abbreviation of what TlsConnection does:
        
        // 1. set up the trust manager (our tls policy)
        
        // 2. create the ssl context
        /**
         * NOTE: A new SSLContext object encapsulating the SSLContextSpi implementation from the first Provider that supports the specified protocol is returned.
         * 
         * Reference:
         * http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLContext.html#getInstance(java.lang.String)
         */
        SSLContext sslContext = SSLContext.getInstance("TLS"); // for example: SSL, SSLv2, SSLv3, TLS, TLSv1
        KeyManager[] kms = null;
        TrustManager[] tms = new TrustManager[] { tlsPolicy.getTrustManager() };
        sslContext.init(kms, tms, new java.security.SecureRandom());
        // 3. create the ssl socket factory
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        // 4. create the ssl socket 
        URL url = new URL(String.format("https://%s:%d",host.getName(), host.getPort()));
        URLConnection connection = url.openConnection(); // throws IOException
        if( connection instanceof HttpsURLConnection ) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection)connection;
            httpsConnection.setSSLSocketFactory(sslSocketFactory);
            httpsConnection.setHostnameVerifier(tlsPolicy.getHostnameVerifier()); // the hostname verifier is only called IF the destination hostname does not match the server certificate.... so java does its own check first, and only if it fails does it call our own verifier.  // TlsPolicyManager.getInstance()); // javax.net.ssl.HttpsURLConnection$DefaultHostnameVerifier unless we set it to com.intel.dcsg.cpg.tls.policy.TlsPolicyManager  but it doesn't get called anymore in java7... it did in java6... 
//            httpsConnection.setHostnameVerifier(new DenyAllHostnameVerifier());
//            httpsConnection.setHostnameVerifier(new AllowAllHostnameVerifier());
        }
        
        
        readConnection((HttpsURLConnection)connection);
        
        
        // check for saved server public key in repository
        printRepository(repository);
    }
    
    /**
     * Even though the method is called "getInstance" it does return a new
     * instance each time - I would have called it "newInstance" or "createInstance"
     * @throws NoSuchAlgorithmException 
     */
    @Test
    public void testDistinctSSLContextInstance() throws NoSuchAlgorithmException {
        SSLContext sslContext1 = SSLContext.getInstance("TLS"); // for example: SSL, SSLv2, SSLv3, TLS, TLSv1
        SSLContext sslContext2 = SSLContext.getInstance("TLS"); // for example: SSL, SSLv2, SSLv3, TLS, TLSv1
        assertFalse(sslContext1 == sslContext2);
        assertNotEquals(sslContext1.hashCode(), sslContext2.hashCode());
    }
    
    /**
     * The "getSocketFactory" method also returns a new instance of an
     * SSLSocketFactory each time it is called. That way changes to the SSLContext
     * are reflected each time it is obtained.
     * 
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException 
     */
    @Test
    public void testDistinctSSLSocketFactoryInstance() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext1 = SSLContext.getInstance("TLS"); // for example: SSL, SSLv2, SSLv3, TLS, TLSv1
        SSLContext sslContext2 = SSLContext.getInstance("TLS"); // for example: SSL, SSLv2, SSLv3, TLS, TLSv1
        sslContext1.init(null, null, null);
        sslContext2.init(null, null, null);
        SSLSocketFactory sslSocketFactory1a = sslContext1.getSocketFactory();
        SSLSocketFactory sslSocketFactory1b = sslContext1.getSocketFactory();
        SSLSocketFactory sslSocketFactory2a = sslContext2.getSocketFactory();
        SSLSocketFactory sslSocketFactory2b = sslContext2.getSocketFactory();
        assertFalse(sslSocketFactory1a == sslSocketFactory1b);
        assertNotEquals(sslSocketFactory1a.hashCode(), sslSocketFactory1b.hashCode());
        assertFalse(sslSocketFactory1a == sslSocketFactory2a);
        assertNotEquals(sslSocketFactory1a.hashCode(), sslSocketFactory2a.hashCode());
        assertFalse(sslSocketFactory2a == sslSocketFactory2b);
        assertNotEquals(sslSocketFactory2a.hashCode(), sslSocketFactory2b.hashCode());
    }
    
    private void readConnection(HttpsURLConnection connection) throws IOException {
        /**
         * Reference: http://docs.oracle.com/javase/tutorial/networking/urls/connecting.html
         */
        connection.connect(); // will either open it the first time or it will be ignored
        log.debug("cipher suite: {}", connection.getCipherSuite()); // for example: TLS_RSA_WITH_AES_128_CBC_SHA
        log.debug("hostname verifier: {}", connection.getHostnameVerifier().getClass().getName()); // for example: org.apache.http.conn.ssl.AllowAllHostnameVerifier
        log.debug("ssl socket factory: {}", connection.getSSLSocketFactory().getClass().getName()); // for example: sun.security.ssl.SSLSocketFactoryImpl
        try(InputStream in = connection.getInputStream()) {
            String content = IOUtils.toString(in);
            log.debug("content: {}", content);
        }
        catch(Exception e) {
            log.debug("error: {}", e.getMessage()); // for example we would typically get 401 unauthorized error from trust agent if we don't specify username and password -- but that's ok because we are testing the tls policy here 
        }
    }
    
    private void printRepository(PublicKeyRepository repository) {
        List<PublicKey> list = repository.getPublicKeys();
        log.debug("Repository has {} public keys", list.size());
        for(PublicKey publicKey : list) {
            log.debug("Public key: {}", Base64.encodeBase64String(publicKey.getEncoded()));
        }
    }

    private void printRepository(CertificateRepository repository) {
        List<X509Certificate> list = repository.getCertificates();
        log.debug("Repository has {} certificates", list.size());
        for(X509Certificate certificate : list) {
            log.debug("Certificate: {}", certificate.getSubjectX500Principal().getName());
        }
    }
    
}
