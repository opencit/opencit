/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.ConsoleTrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.ArrayCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.HashSetMutablePublicKeyRepository;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
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
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
//import org.mortbay.jetty.Server;
//import org.mortbay.jetty.handler.ContextHandlerCollection;
//import org.mortbay.jetty.security.SslSocketConnector;

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
    private static String serverAddress = "localhost";
    private static String keystorePassword = "password";
    private static String caKeystorePath;
    public static int serverPort = 17443;
    private static Server jetty;
    private static URL httpsURL;
    public static X509Certificate caCert;  // the CA cert that signs the test server's TLS cert
    public static X509Certificate tlsCert; // the test server's TLS cert
    
    public static KeyPair generateRsaKeyPair(int keySizeInBits) throws NoSuchAlgorithmException {
        KeyPairGenerator r = KeyPairGenerator.getInstance("RSA");
        r.initialize(keySizeInBits);
        KeyPair keypair = r.generateKeyPair();
        return keypair;
    }
    
    public static void createKeystoreWithCredential(File keystoreFile, PrivateKey privateKey, X509Certificate certificate) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null, keystorePassword.toCharArray());
        keystore.setKeyEntry(certificate.getSubjectX500Principal().getName(), privateKey, keystorePassword.toCharArray(), new X509Certificate[] { certificate });
//        keystore.setCertificateEntry(certificate.getSubjectX500Principal().getName(), certificate); // unnecessary, certificate is already set with setKeyEntry;   if you try to do this you would get KeyStoreException: Cannot overwrite own certificate
        FileResource keystoreResource = new FileResource(keystoreFile);
        OutputStream out = keystoreResource.getOutputStream();
        keystore.store(out, keystorePassword.toCharArray());
        out.close();        
    }

    private static void createTestCa(String filename) throws NoSuchAlgorithmException, CertificateEncodingException, KeyManagementException, KeyStoreException, IOException, CertificateException {
        System.out.println("createTestCa");
        // create ca cert
        KeyPair caKeys = generateRsaKeyPair(1024);
        caCert = X509Builder.factory().selfSigned("CN=testca", caKeys).expires(30, TimeUnit.DAYS).keyUsageCertificateAuthority().build();
        System.out.println("Created CA cert with CA flag: "+caCert.getBasicConstraints());
        // RsaCredentialX509 ca = new RsaCredentialX509(caKeys.getPrivate(), caCert);
//        System.out.println("CA CERT PEM:");
//        System.out.println(X509Util.encodePemCertificate(caCert));  // weird:   getBasicConstraints() above returns -1, when it should return Integer.MAX_VALUE according to the javadoc... writing cert to file as PEM and reading with java keytool shows the CA basic constraint with path length Integer.MAX_VALUE, so we know the certificate is generated correctly... not sure why it's not giving the right value here for getBasicConstraints() ... which is causing X509Util.isCA():booelan to return false incorrectly for the CA cert.
        // create tls cert
        KeyPair tlsKeys = generateRsaKeyPair(1024);
        tlsCert = X509Builder.factory().subjectName("CN=testserver,O=ca-signed").dnsAlternativeName("localhost").subjectPublicKey(tlsKeys.getPublic()).issuerName(caCert).issuerPrivateKey(caKeys.getPrivate()).expires(30, TimeUnit.DAYS).keyUsageDataEncipherment().build();
        System.out.println("Created TLS cert with CA flag: "+tlsCert.getBasicConstraints());
        // save both into the server's keystore
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null, keystorePassword.toCharArray());
        keystore.setKeyEntry(tlsCert.getSubjectX500Principal().getName(), tlsKeys.getPrivate(), keystorePassword.toCharArray(), new X509Certificate[] { tlsCert });
        createKeystoreWithCredential(new File(filename), tlsKeys.getPrivate(), tlsCert);
        keystore.setCertificateEntry(caCert.getSubjectX500Principal().getName(), caCert); // if you add the CA to the keystore, must add it as a certificate and not the full credential with private key, because then the web server will not know which private key to use and may send the ca instead of the server cert...   other servers like glassfish may allow you to select the alias of the server cert to use . looks like jetty does not.
        // create a bogus ca cert and include it in the keystore... let's see what the trust manager gets
//        KeyPair caKeys2 = RsaUtil.generateRsaKeyPair(1024);
//        X509Certificate caCert2 = X509Builder.factory().selfSigned("CN=testca2", caKeys2).expires(30, TimeUnit.DAYS).keyUsageCertificateAuthority().build();
//        RsaCredentialX509 ca2 = new RsaCredentialX509(caKeys2.getPrivate(), caCert2);
//        keystore.setRsaCredentialX509(ca2, "ca2", "password"); // looks like if the ca signed the tls cert (normal case) only the ca is sent to the x509trustmanager, which would be ok except that it's also sent to the hostnameverifier which does NOT make sense (the CA is not going to have the hostname in it!!!)
        FileResource keystoreResource = new FileResource(new File(filename));
        OutputStream out = keystoreResource.getOutputStream();
        keystore.store(out, keystorePassword.toCharArray());
        out.close();        
    }
    
    private static void createSelfSigned(String filename) throws NoSuchAlgorithmException, CertificateEncodingException, KeyManagementException, KeyStoreException, IOException, CertificateException {
        System.out.println("createSelfSigned");
        KeyPair caKeys = generateRsaKeyPair(1024);
        tlsCert = caCert = X509Builder.factory().selfSigned("CN=testserver,O=self-signed", caKeys).dnsAlternativeName("localhost").expires(30, TimeUnit.DAYS).keyUsageDataEncipherment().build(); // the dns alternative name is required if the cilent uses the CA policy (because it requires matching hostname) 
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null, keystorePassword.toCharArray());
        keystore.setKeyEntry(tlsCert.getSubjectX500Principal().getName(), caKeys.getPrivate(), keystorePassword.toCharArray(), new X509Certificate[] { caCert });
        FileResource keystoreResource = new FileResource(new File(filename));
        OutputStream out = keystoreResource.getOutputStream();
        keystore.store(out, keystorePassword.toCharArray());
        out.close();        
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
        
        
        jetty = new Server(serverPort-1);   // jetty will listen for http on serverPort-1, and https on serverPort
        jetty.setHandler(new HelloWorldHandler());
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
        TlsPolicy tlsPolicyInsecure = new InsecureTlsPolicy();
        HttpClient httpClient = httpClientFactoryCreateSslClientWithPolicy(tlsPolicyInsecure); //new DefaultHttpClient();
        
        HttpGet request = new HttpGet(httpsURL.toExternalForm());
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        httpClient.getConnectionManager().shutdown();
    }
    @Test
    public void testTrustKnownCertificateClient() throws Exception {
        HashSetMutablePublicKeyRepository repository = new HashSetMutablePublicKeyRepository();
        repository.addPublicKey(tlsCert.getPublicKey());
        TlsPolicy tlsPolicyTrustKnownCertificate = new PublicKeyTlsPolicy(repository);
        HttpClient httpClient = httpClientFactoryCreateSslClientWithPolicy(tlsPolicyTrustKnownCertificate); //new DefaultHttpClient();
        
        HttpGet request = new HttpGet(httpsURL.toExternalForm());
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        httpClient.getConnectionManager().shutdown();
    }

    @Test(expected=SSLPeerUnverifiedException.class)
    public void testTrustUnknownCertificateClient() throws Exception {
        HashSetMutablePublicKeyRepository repository = new HashSetMutablePublicKeyRepository();
        TlsPolicy tlsPolicyTrustKnownCertificate = new PublicKeyTlsPolicy(repository);
        HttpClient httpClient = httpClientFactoryCreateSslClientWithPolicy(tlsPolicyTrustKnownCertificate); //new DefaultHttpClient();
        
        HttpGet request = new HttpGet(httpsURL.toExternalForm());
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        httpClient.getConnectionManager().shutdown();
    }

//    @Test
    public void testTrustUnnownCertificateClientWithDelegate() throws Exception {
        HashSetMutablePublicKeyRepository repository = new HashSetMutablePublicKeyRepository();
        TlsPolicy tlsPolicyTrustKnownCertificate = new PublicKeyTlsPolicy(repository, new ConsoleTrustDelegate(repository));
        HttpClient httpClient = httpClientFactoryCreateSslClientWithPolicy(tlsPolicyTrustKnownCertificate); //new DefaultHttpClient();
        
        HttpGet request = new HttpGet(httpsURL.toExternalForm());
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        httpClient.getConnectionManager().shutdown();
    }
    
    @Test
    public void testTrustCaCertificateClient() throws Exception {
        TlsPolicy tlsPolicyTrustCaAndVerifyHostname = new CertificateTlsPolicy(new ArrayCertificateRepository(new X509Certificate[] { caCert }));
        HttpClient httpClient = httpClientFactoryCreateSslClientWithPolicy(tlsPolicyTrustCaAndVerifyHostname); //new DefaultHttpClient();
        
        HttpGet request = new HttpGet(httpsURL.toExternalForm());
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        httpClient.getConnectionManager().shutdown();
    }


    @Test
    public void testTrustCaCertificateClientWithKnownCertPolicy() throws Exception {
        TlsPolicy tlsPolicyTrustCaAndVerifyHostname = new CertificateTlsPolicy(new ArrayCertificateRepository(new X509Certificate[] { tlsCert }));
        HttpClient httpClient = httpClientFactoryCreateSslClientWithPolicy(tlsPolicyTrustCaAndVerifyHostname); //new DefaultHttpClient();
        
        HttpGet request = new HttpGet(httpsURL.toExternalForm());
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        httpClient.getConnectionManager().shutdown();
    }
    
    private HttpClient httpClientFactoryCreateSslClientWithPolicy(TlsPolicy tlsPolicy) throws KeyManagementException, NoSuchAlgorithmException {
        SchemeRegistry sr = initSchemeRegistry("https", serverPort, tlsPolicy);
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(sr);

        // the http client is re-used for all the requests
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
        DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, httpParams);
        return httpClient;
    }
    
    // from ApacheHttpClient in api-client-jar project;  TODO: refactor into a factory class to create an http client with given certificate repository and tls policy
    private SchemeRegistry initSchemeRegistry(String protocol, int port, TlsPolicy policy) throws KeyManagementException, NoSuchAlgorithmException {
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
                policy.getHostnameVerifier()
                );
            Scheme https = new Scheme("https", port, sf); // URl defaults to 443 for https but if user specified a different port we use that instead
            sr.register(https);            
        }        
        return sr;
    }
   
}
