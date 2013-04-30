/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls;

import com.intel.mtwilson.crypto.RsaCredentialX509;
import com.intel.mtwilson.crypto.RsaUtil;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.crypto.X509Builder;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.model.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.security.SslSocketConnector;

/**
 * Sample http output from Jetty configured with no content handlers:
HTTP/1.1 404 Not Found
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<title>Error 404 NOT_FOUND</title>
</head>
<body>
<h2>HTTP ERROR: 404</h2>
<p>Problem accessing /. Reason:
<pre>    NOT_FOUND</pre></p>
<hr /><i><small>Powered by Jetty://</small></i>
</body>
</html>
 * 
 * @author jbuhacoff
 */
public class TestTlsPolicyWithServerCaSigned {
    private static InternetAddress serverAddress = new InternetAddress("localhost");
    private static String caKeystorePath;
    private static int serverPort = 17443;
    private static Server jetty;
    private static URL httpsURL;
    private static X509Certificate caCert;
    private static X509Certificate tlsCert;
    
    private static void createTestCa(String filename) throws NoSuchAlgorithmException, CertificateEncodingException, KeyManagementException, KeyStoreException, IOException, CertificateException {
        System.out.println("createTestCa");
        // create ca cert
        KeyPair caKeys = RsaUtil.generateRsaKeyPair(1024);
        caCert = X509Builder.factory().selfSigned("CN=testca", caKeys).expires(30, TimeUnit.DAYS).keyUsageCertificateAuthority().build();
        System.out.println("Created CA cert with CA flag: "+caCert.getBasicConstraints());
        // RsaCredentialX509 ca = new RsaCredentialX509(caKeys.getPrivate(), caCert);
//        System.out.println("CA CERT PEM:");
//        System.out.println(X509Util.encodePemCertificate(caCert));  // weird:   getBasicConstraints() above returns -1, when it should return Integer.MAX_VALUE according to the javadoc... writing cert to file as PEM and reading with java keytool shows the CA basic constraint with path length Integer.MAX_VALUE, so we know the certificate is generated correctly... not sure why it's not giving the right value here for getBasicConstraints() ... which is causing X509Util.isCA():booelan to return false incorrectly for the CA cert.
        // create tls cert
        KeyPair tlsKeys = RsaUtil.generateRsaKeyPair(1024);
        tlsCert = X509Builder.factory().subjectName("CN=testserver,O=ca-signed").dnsAlternativeName("localhost").subjectPublicKey(tlsKeys.getPublic()).issuerName(caCert).issuerPrivateKey(caKeys.getPrivate()).expires(30, TimeUnit.DAYS).keyUsageDataEncipherment().build();
        System.out.println("Created TLS cert with CA flag: "+tlsCert.getBasicConstraints());
        RsaCredentialX509 tls = new RsaCredentialX509(tlsKeys.getPrivate(), tlsCert);
        // save both into the server's keystore
        SimpleKeystore keystore = new SimpleKeystore(new File(filename), "password");
        keystore.setRsaCredentialX509(tls, "server", "password");
//        keystore.setRsaCredentialX509(ca, "ca", "password"); // looks like if the ca signed the tls cert (normal case) only the ca is sent to the x509trustmanager, which would be ok except that it's also sent to the hostnameverifier which does NOT make sense (the CA is not going to have the hostname in it!!!)
        keystore.addTrustedCaCertificate(caCert, "ca"); // if you add the CA to the keystore, must add it as a certificate and not the full credential with private key, because then the web server will not know which private key to use and may send the ca instead of the server cert...   other servers like glassfish may allow you to select the alias of the server cert to use . looks like jetty does not.
        // create a bogus ca cert and include it in the keystore... let's see what the trust manager gets
//        KeyPair caKeys2 = RsaUtil.generateRsaKeyPair(1024);
//        X509Certificate caCert2 = X509Builder.factory().selfSigned("CN=testca2", caKeys2).expires(30, TimeUnit.DAYS).keyUsageCertificateAuthority().build();
//        RsaCredentialX509 ca2 = new RsaCredentialX509(caKeys2.getPrivate(), caCert2);
//        keystore.setRsaCredentialX509(ca2, "ca2", "password"); // looks like if the ca signed the tls cert (normal case) only the ca is sent to the x509trustmanager, which would be ok except that it's also sent to the hostnameverifier which does NOT make sense (the CA is not going to have the hostname in it!!!)
        
        keystore.save();
    }
    
    private static void createSelfSigned(String filename) throws NoSuchAlgorithmException, CertificateEncodingException, KeyManagementException, KeyStoreException, IOException, CertificateException {
        System.out.println("createSelfSigned");
        KeyPair caKeys = RsaUtil.generateRsaKeyPair(1024);
        tlsCert = caCert = X509Builder.factory().selfSigned("CN=testserver,O=self-signed", caKeys).dnsAlternativeName("localhost").expires(30, TimeUnit.DAYS).keyUsageDataEncipherment().build(); // the dns alternative name is required if the cilent uses the CA policy (because it requires matching hostname) 
        RsaCredentialX509 ca = new RsaCredentialX509(caKeys.getPrivate(), caCert);
        SimpleKeystore keystore = new SimpleKeystore(new File(filename), "password");
        keystore.setRsaCredentialX509(ca, "server", "password");
        keystore.save();
    }
    
    @BeforeClass
    public static void startJetty() throws Exception {
        // randomize server port because sometimes it doesn't shut down cleanly from a previous test, and we don't want that to interfere
        Random rnd = new Random();
        serverPort += rnd.nextInt(10000);
        httpsURL = new URL("https://localhost:"+serverPort);
        System.out.println("Starting Jetty on port "+serverPort);
        // create a CA signed cert for the server
        caKeystorePath = System.getProperty("java.io.tmpdir")+File.separator+"ca"+serverPort+".jks"; // with port number to ensure we create a different file each time, in  case the contents need to change
        createTestCa(caKeystorePath);
//        createSelfSigned(caKeystorePath);
        System.out.println("Server keystore: "+caKeystorePath);
        
        
        jetty = new Server(serverPort-10000);  // jetty will listen for http on serverPort-10000
        ContextHandlerCollection collection = new ContextHandlerCollection();
        jetty.setHandler(collection);
        SslSocketConnector sslConnector = new SslSocketConnector();
        sslConnector.setKeystore(caKeystorePath);
        sslConnector.setKeyPassword("password");
        sslConnector.setTruststore(caKeystorePath);
        sslConnector.setTrustPassword("password");
        sslConnector.setPort(serverPort); // jetty will listen for https on serverPort
        jetty.addConnector(sslConnector);
        jetty.start();
    }
    
    @AfterClass
    public static void stopJetty() throws Exception {
        jetty.stop();
    }
    
    @Test
    public void testInsecureClient() throws Exception {
        // prepare an empty keystore
        //ByteArrayResource resource = new ByteArrayResource();
        //SimpleKeystore keystore = new SimpleKeystore(resource, "password");

        ApacheTlsPolicy tlsPolicyInsecure = new InsecureTlsPolicy();
        HttpClient httpClient = httpClientFactoryCreateSslClientWithPolicy(tlsPolicyInsecure); //new DefaultHttpClient();
        
        HttpGet request = new HttpGet(httpsURL.toExternalForm());
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        httpClient.getConnectionManager().shutdown();
    }
    
    @Test
    public void testTrustKnownCertificateClient() throws Exception {
        ApacheTlsPolicy tlsPolicyTrustKnownCertificate = new TrustKnownCertificateTlsPolicy(/*serverAddress, */new ArrayCertificateRepository(new X509Certificate[] { tlsCert }));; // does not validate: need the ca cert in the keystore
        HttpClient httpClient = httpClientFactoryCreateSslClientWithPolicy(tlsPolicyTrustKnownCertificate); //new DefaultHttpClient();
        
        HttpGet request = new HttpGet(httpsURL.toExternalForm());
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        httpClient.getConnectionManager().shutdown();
    }

    
    @Test
    public void testTrustFirstCertificateClient() throws Exception {
        // create a keystore for the client
        ByteArrayResource resource = new ByteArrayResource();
        SimpleKeystore clientKeystore = new SimpleKeystore(resource, "password");
        ApacheTlsPolicy tlsPolicyTrustFirstCertificate = new TrustFirstCertificateTlsPolicy(/*serverAddress, */new KeystoreCertificateRepository(clientKeystore));;
        HttpClient httpClient = httpClientFactoryCreateSslClientWithPolicy(tlsPolicyTrustFirstCertificate); //new DefaultHttpClient();
        
        HttpGet request = new HttpGet(httpsURL.toExternalForm());
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        
        // now show content of repository:
        System.out.println("Certificate repository after first request:");
        String[] aliases = clientKeystore.aliases();
        for(String alias : aliases) {
            X509Certificate cert = clientKeystore.getX509Certificate(alias);
            System.out.println("- "+cert.getSubjectX500Principal().getName());
        }
        
        // now make another request and make sure it' doesn't get added
        HttpResponse response2 = httpClient.execute(request);
        System.out.println(response2.getStatusLine());
        System.out.println(IOUtils.toString(response2.getEntity().getContent()));
        
        System.out.println("Certificate repository after second request:");
        String[] aliases2 = clientKeystore.aliases();
        for(String alias : aliases2) {
            X509Certificate cert = clientKeystore.getX509Certificate(alias);
            System.out.println("- "+cert.getSubjectX500Principal().getName());
        }
        
        
        httpClient.getConnectionManager().shutdown();
    }
    
    @Test
    public void testTrustCaCertificateClient() throws Exception {
        ApacheTlsPolicy tlsPolicyTrustCaAndVerifyHostname = new TrustCaAndVerifyHostnameTlsPolicy(new ArrayCertificateRepository(new X509Certificate[] { caCert }));
        HttpClient httpClient = httpClientFactoryCreateSslClientWithPolicy(tlsPolicyTrustCaAndVerifyHostname); //new DefaultHttpClient();
        
        HttpGet request = new HttpGet(httpsURL.toExternalForm());
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        httpClient.getConnectionManager().shutdown();
    }

    
    private HttpClient httpClientFactoryCreateSslClientWithPolicy(ApacheTlsPolicy tlsPolicy) throws KeyManagementException, NoSuchAlgorithmException {
        SchemeRegistry sr = initSchemeRegistry("https", serverPort, tlsPolicy);
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(sr);

        // the http client is re-used for all the requests
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
        DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, httpParams);
        return httpClient;
    }
    
    // from ApacheHttpClient in api-client-jar project;  TODO: refactor into a factory class to create an http client with given certificate repository and tls policy
    private SchemeRegistry initSchemeRegistry(String protocol, int port, ApacheTlsPolicy policy) throws KeyManagementException, NoSuchAlgorithmException {
        SchemeRegistry sr = new SchemeRegistry();
        if( "http".equals(protocol) ) {
            Scheme http = new Scheme("http", port, PlainSocketFactory.getSocketFactory());
            sr.register(http);
        }
        if( "https".equals(protocol) ) {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new X509TrustManager[] { policy.getTrustManager() }, null); // key manager, trust manager, securerandom
            SSLSocketFactory sf = new SSLSocketFactory(
                sslcontext,
                policy.getApacheHostnameVerifier()
                );
            Scheme https = new Scheme("https", port, sf); // URl defaults to 443 for https but if user specified a different port we use that instead
            sr.register(https);            
        }        
        return sr;
    }
    
}
