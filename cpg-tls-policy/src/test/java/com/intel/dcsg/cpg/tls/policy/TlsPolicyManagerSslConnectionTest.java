/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.tls.policy.impl.ApacheTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.StrictTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.TrustKnownCertificateTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.ArrayCertificateRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This tests use of the TlsPolicyManager with an SSL connection that does
 * NOT use URLConnection so only the trust manager is used and not the
 * hostnaem verifier. 
 * The two options are for the caller to use the hostname verifier directly
 * or for the TlsPolicyManager to support a mode where the verification 
 * can be tied to the address (the Java API for the trust manager only
 * presents the server certificates but not the address being accessed).
 * @author jbuhacoff
 */
public class TlsPolicyManagerSslConnectionTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyManagerSslConnectionTest.class);
    private static final String data = "GET /index.html HTTP/1.1\r\nHost: localhost\r\nAccept: */*\r\n";
    private static int TIME_OUT = 1000; // milliseconds
            
    @BeforeClass
    public static void startJetty() throws Exception {
        TestTlsPolicyWithServerCaSigned.startJetty();
    }
    
    @AfterClass
    public static void stopJetty() throws Exception {
        TestTlsPolicyWithServerCaSigned.stopJetty();
    }
    
    @Test
    public void testSslConnectionInsecurePolicy() throws Exception {
        TlsPolicy tlsPolicy = new InsecureTlsPolicy();
        String serverHostname = "127.0.0.1";
        int serverPort = TestTlsPolicyWithServerCaSigned.serverPort;
        connect(serverHostname,serverPort, tlsPolicy);
    }
    
    @Test
    public void testSslConnectionKnownCertificatePolicy() throws Exception {
        TlsPolicy tlsPolicyTrustKnownCertificate = new TrustKnownCertificateTlsPolicy(new ArrayCertificateRepository(new X509Certificate[] { TestTlsPolicyWithServerCaSigned.tlsCert }));
        String serverHostname = "127.0.0.1";
        int serverPort = TestTlsPolicyWithServerCaSigned.serverPort;
        connect(serverHostname,serverPort, tlsPolicyTrustKnownCertificate);
    }
    
//    @Test(expected=SSLPeerUnverifiedException.class)
    @Test(expected=SSLHandshakeException.class)
    public void testSslConnectionKnownCertificatePolicyIncorrectCert() throws Exception {
        TlsPolicy tlsPolicyTrustKnownCertificate = new TrustKnownCertificateTlsPolicy(new ArrayCertificateRepository(new X509Certificate[] {  }));
        String serverHostname = "127.0.0.1";
        int serverPort = TestTlsPolicyWithServerCaSigned.serverPort;
        connect(serverHostname,serverPort, tlsPolicyTrustKnownCertificate);
    }

    /**
     * This one works even with a non-matching hostname because KnownCertificatePolicy does not check hostnames, only the certificate trust
     * @throws Exception 
     */
    @Test
    public void testSslConnectionKnownCertificatePolicyWithHostnameVerification() throws Exception {
        TlsPolicy tlsPolicy = new TrustKnownCertificateTlsPolicy(new ArrayCertificateRepository(new X509Certificate[] { TestTlsPolicyWithServerCaSigned.tlsCert }));
        String serverHostname = "127.0.0.1"; // the certificate says "localhost" so this would fail if we did hostname verification, but known certificate policy doesn't so it's ko
        int serverPort = TestTlsPolicyWithServerCaSigned.serverPort;
        connectWithHostnameVerification(serverHostname,serverPort, tlsPolicy);
    }
    
    /**
     * Test of the strict tls policy (certificate trust and hostname verification)  ... server cert says "localhost" but we're connecting to "127.0.0.1" so it will throw an exception
     * @throws Exception 
     */
    @Test(expected=SSLPeerUnverifiedException.class)
    public void testSslConnectionStrictCertificatePolicyWithHostnameVerificationAndNonmatchingAddress() throws Exception {
        TlsPolicy tlsPolicy = new StrictTlsPolicy(new ArrayCertificateRepository(new X509Certificate[] { TestTlsPolicyWithServerCaSigned.tlsCert }));
        String serverHostname = "127.0.0.1"; // the certificate says "localhost" so this will fail with "Invalid certificate for address: 127.0.0.1
        int serverPort = TestTlsPolicyWithServerCaSigned.serverPort;
        connectWithHostnameVerification(serverHostname,serverPort, tlsPolicy);
    }

    /**
     * Test of the strict tls policy (certificate trust and hostname verification)  ... server cert says "localhost" and we're connecting to "localhost" so it's ok
     * @throws Exception 
     */
    @Test
    public void testSslConnectionStrictCertificatePolicyWithHostnameVerificationAndMatchingAddress() throws Exception {
        TlsPolicy tlsPolicy = new StrictTlsPolicy(new ArrayCertificateRepository(new X509Certificate[] { TestTlsPolicyWithServerCaSigned.tlsCert }));
        String serverHostname = "localhost"; // the certificate says "localhost" so this will match
        int serverPort = TestTlsPolicyWithServerCaSigned.serverPort;
        connectWithHostnameVerification(serverHostname,serverPort, tlsPolicy);
    }
    
    public void connectWithHostnameVerification(String address, int port, TlsPolicy tlsPolicy) throws Exception {
        URL url = new URL(String.format("tcp://%s:%d", address, port));
        TlsConnection tlsConnection = new TlsConnection(url, tlsPolicy);
        SSLSocket socket = tlsConnection.connect(TIME_OUT);
        talk(socket);
        socket.close();                
    }
        
    public void connect(String address, int port, TlsPolicy tlsPolicy) throws IOException, NoSuchAlgorithmException, KeyManagementException {
    	SSLContext ctx = SSLContext.getInstance("SSL");
        ctx.init(null, new javax.net.ssl.TrustManager[]{ tlsPolicy.getTrustManager() }, null);
        SSLSocketFactory sslsocketfactory = ctx.getSocketFactory();
        SSLSocket sock = (SSLSocket) sslsocketfactory.createSocket();
        sock.connect(new InetSocketAddress(address,port), TIME_OUT);
        talk(sock);
        sock.close();        
    }
    
    public void talk(SSLSocket sock) throws IOException {
            OutputStream sockOutput = sock.getOutputStream();

            log.info("About to start reading/writing to/from socket.");
            log.debug("Writing: {}", data);
                sockOutput.write(data.getBytes());
                sockOutput.flush();
//            InputStream sockInput = sock.getInputStream();
//            byte[] buf = new byte[5000];
//            int bytes_read = sockInput.read(buf);
//            log.debug( "Received {} bytes to server and received them back again, msg = {} ", bytes_read , new String(buf));
                
    }
}
