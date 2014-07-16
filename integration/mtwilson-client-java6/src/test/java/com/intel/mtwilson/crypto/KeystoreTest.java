/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.crypto.HmacCredential;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.datatypes.OsData;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.ConfigurationUtil;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.mtwilson.datatypes.Role;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * These tests use a temporary file for the keystore and use an embedded
 * configuration to control the tests. To bootstrap your
 * developer environment please use the code in the "Bootstrap" class.
 * @author jbuhacoff
 */
public class KeystoreTest {
    public static Properties config;
    
    @BeforeClass
    public static void configure() throws IOException {
        // create a temporary keystore file for this test suite
        File tmp = File.createTempFile("keystore", ".jks"); // IOException.  // creates a temporary file
        config = new Properties();
        config.setProperty("mtwilson.api.baseurl", "https://10.1.71.134:8181");
        config.setProperty("mtwilson.api.keystore", tmp.getAbsolutePath());
        config.setProperty("mtwilson.api.keystore.password", "changeit123");
        config.setProperty("mtwilson.api.key.alias", "mysecretapikey");
        config.setProperty("mtwilson.api.key.password", "changeit123");
        config.setProperty("mtwilson.api.ssl.verifyHostname", "false");
        config.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
    }
    
    /* 
     * Performs an arbitrary api call, "listAllOS"...
     * Fails if the keystore does not have the server's SSL certificate
     */
    @Test
    public void executeApiCall() throws MalformedURLException, ClientException, IOException, IOException, ApiException, ApiException, SignatureException {
        // test API client against a server and require valid certs
        URL url = new URL(config.getProperty("mtwilson.api.baseurl"));
        Properties p = new Properties();
        p.setProperty("mtwilson.api.keystore", config.getProperty("mtwilson.api.keystore"));
        p.setProperty("mtwilson.api.keystore.password", config.getProperty("mtwilson.api.keystore.password"));
        p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
        p.setProperty("mtwilson.api.ssl.verifyHostname", "false");
        ApiClient c = new ApiClient(url, new HmacCredential("cloudportal@intel","nU8jTeJaFJZ7TJdMb4g4wAOljEHqwyFoRGvrsPjxrST8icOU"), p);
        List<OsData> list = c.listAllOS();
        System.out.println(String.format("Got list size %i", list.size()));                    
    }

    @Test
    public void createNewKeystore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        File keystoreFile = new File(config.getProperty("mtwilson.api.keystore"));
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null, null);
        KeystoreUtil.save(keystore, config.getProperty("mtwilson.api.keystore.password"), keystoreFile);
        System.out.println("Created new keystore in "+keystoreFile.getAbsolutePath());
    }
    
    
    @Test
    public void showCertificatesInKeystore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, UnrecoverableEntryException  {
        KeyStore keystore = KeystoreUtil.open(new MapConfiguration(config));
        Enumeration<String> aliases = keystore.aliases();
        System.out.println("Certificates in keystore:");
        int count = 0;
        while(aliases.hasMoreElements()) {
            count++;
            String alias = aliases.nextElement();
            System.out.println("Trusted certificate: "+alias);
            X509Certificate cert = KeystoreUtil.loadX509Certificate(keystore, alias);
            System.out.println("Subject: "+cert.getSubjectX500Principal().getName());
        }
        System.out.println(String.format("%d certificates", count));
    }
    
    /**
     * Saves server's SSL certificates into keystore - assumes they are trusted.
     * This is only sample code, in production you need to prompt the user to
     * verify the fingerprint of the certificate before you add it, to prevent
     * man-in-the-middle attacks.
     * Since these are SSL certificates they are added to the keystore with the
     * "SSL" trusted purpose tag in their alias. 
     * @throws MalformedURLException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException 
     */
    @Test
    public void addServerSslCertificateToKeystore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException  {
        KeyStore keystore = KeystoreUtil.open(new MapConfiguration(config));
        URL url = new URL(config.getProperty("mtwilson.api.baseurl"));        
        X509Certificate[] certs = TlsUtil.getServerCertificates(url);
        String aliasBasename = "serverCert";
        int certificateNumber = 0;
        for(X509Certificate cert : certs) {
            certificateNumber++;
            String alias = String.format("%s%d (ssl)", aliasBasename, certificateNumber);
            keystore.setCertificateEntry(alias, cert);
            System.out.println("Added certificate to keystore: "+aliasBasename);
        }
        KeystoreUtil.save(keystore, config.getProperty("mtwilson.api.keystore.password"), new File(config.getProperty("mtwilson.api.keystore")));
    }

    @Test
    public void createNewKeystoreWithServerSslCert() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, KeyManagementException  {
        createNewKeystore();
        showCertificatesInKeystore();
        addServerSslCertificateToKeystore();
        showCertificatesInKeystore();
    }
    
    /*
     * Same as createNewKeystoreWithServerSslCert but uses the SimpleKeystore class. 
     */
    @Test
    public void createNewKeystoreWithServerSslCertUsingSimpleKeystore() throws KeyManagementException, KeyStoreException, MalformedURLException, NoSuchAlgorithmException, IOException, CertificateException {
        // create new keystore (if file does not exist and is writable it will be created when we save)
        SimpleKeystore keystore = new SimpleKeystore(new File(config.getProperty("mtwilson.api.keystore")), config.getProperty("mtwilson.api.keystore.password"));
        // list certificates
        String[] aliases1 = keystore.listTrustedSslCertificates();
        for(String alias : aliases1) { System.out.println("Certificate: "+alias); }
        System.out.println(String.format("%d certificates", aliases1.length));
        // add server ssl certificate
        URL url = new URL(config.getProperty("mtwilson.api.baseurl"));        
        X509Certificate[] certs = TlsUtil.getServerCertificates(url);
        String aliasBasename = "serverCert";
        int certificateNumber = 0;
        for(X509Certificate cert : certs) {
            certificateNumber++;
            String alias = String.format("%s%d", aliasBasename, certificateNumber);
            keystore.addTrustedSslCertificate(cert, alias);
            System.out.println("Added certificate to keystore: "+alias);
        }
        // show certificates again
        String[] aliases2 = keystore.listTrustedSslCertificates();
        for(String alias : aliases2) { System.out.println("Certificate: "+alias); }
        System.out.println(String.format("%d certificates", aliases2.length));
        // save keystore
        keystore.save();
    }
    
/*    
    @Test
    public void testCreateKeystoreWithServerSslCertificate() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        // create a new keystore and save the new key into it
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null, null);
        keystore.setKeyEntry("mykey", keypair.getPrivate(), "changeit".toCharArray(), new X509Certificate[] { certificate });
        File tmp = File.createTempFile("keystore", ".jks"); // IOException.  // creates a temporary file
        KeystoreUtil.save(keystore, "changeit", tmp);
        System.out.println("Keystore is in "+tmp.getAbsolutePath());
        
    }
   */ 
    
    @Test
    public void testGetServerSslCerts() throws IOException, NoSuchAlgorithmException, KeyManagementException, CertificateEncodingException {
        X509Certificate[] certs = TlsUtil.getServerCertificates(new URL("https://10.1.71.81:8181/AttestationService"));
        for(X509Certificate cert : certs) {
            System.out.println(String.format("Subject: %s", cert.getSubjectX500Principal().getName()));
            System.out.println(String.format("Issuer: %s", cert.getIssuerX500Principal().getName()));
            System.out.println(String.format("Not Before: %s", cert.getNotBefore().toString()));
            System.out.println(String.format("Not After: %s", cert.getNotAfter().toString()));
            byte[] certBytes = cert.getEncoded();
            MessageDigest hashMd5 = MessageDigest.getInstance("MD5");
            byte[] digestMd5 = hashMd5.digest(certBytes);
            System.out.println(String.format("MD5: %s", new String(Hex.encodeHex(digestMd5))));
            MessageDigest hashSha1 = MessageDigest.getInstance("SHA-1");
            byte[] digestSha1 = hashSha1.digest(certBytes);
            System.out.println(String.format("SHA-1: %s", new String(Hex.encodeHex(digestSha1))));
            MessageDigest hashSha256 = MessageDigest.getInstance("SHA-256");
            byte[] digestSha256 = hashSha256.digest(certBytes);
            System.out.println(String.format("SHA-256: %s", new String(Hex.encodeHex(digestSha256))));
        }
    }
    
    public static void main(String[] args) throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, UnsupportedEncodingException, IOException, ApiException, SignatureException, KeyStoreException, CertificateException, ClientException {
        URL url = new URL("https://10.1.71.81:8181");
        try {
            // test API client against a server and require valid certs
            Properties p = new Properties();
            p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
            p.setProperty("mtwilson.api.ssl.verifyHostname", "false");
            ApiClient c = new ApiClient(url, new HmacCredential("cloudportal@intel","nU8jTeJaFJZ7TJdMb4g4wAOljEHqwyFoRGvrsPjxrST8icOU"), p);
            List<OsData> list = c.listAllOS();
            System.out.println(String.format("Got list size %i", list.size()));            
        }
        catch(javax.net.ssl.SSLPeerUnverifiedException e) {
            System.out.println(String.format("SSL certificate for server %s is not trusted", url.toExternalForm()));
            System.out.println("Add certificate to trust store and try again? (Y/N) ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String saveCertAndTryAgain = in.readLine().trim();
            if( saveCertAndTryAgain.toUpperCase().startsWith("Y") ) {
                // download server SSL certificates
                X509Certificate[] serverCertificates = TlsUtil.getServerCertificates(url);
                // create a new temporary trust store and add those certificates
                KeyStore keystore = KeyStore.getInstance("JKS");
                keystore.load(null, null);
                String aliasBasename = "tmp";
                int certificateNumber = 0;
                for(X509Certificate cert : serverCertificates) {
                    certificateNumber++;
                    String alias = String.format("%s%i", aliasBasename, certificateNumber);
                    keystore.setCertificateEntry(alias, cert);
                }
                File tmp = File.createTempFile("keystore", ".jks"); // IOException.  // creates a temporary file
                KeystoreUtil.save(keystore, "changeit", tmp);
                System.out.println("Keystore is in "+tmp.getAbsolutePath());
                // try the call again
                Properties p = new Properties();
                p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
                p.setProperty("mtwilson.api.ssl.verifyHostname", "false");
                p.setProperty("mtwilson.api.truststore", tmp.getAbsolutePath()); 
                ApiClient c = new ApiClient(url, new HmacCredential("cloudportal@intel","nU8jTeJaFJZ7TJdMb4g4wAOljEHqwyFoRGvrsPjxrST8icOU"), p);
                List<OsData> list = c.listAllOS();
                System.out.println(String.format("Got list size %i", list.size()));            
                
            }
        }
        /*
         * 
mtwilson.api.clientId=
mtwilson.api.secretKey=
         * 
        System.out.println("URL: "+baseurl.toExternalForm());
        System.out.print("Root password: ");
         * 
         */
    }
    
    @Test
    public void testCreateUserInResource() throws MalformedURLException, IOException, ApiException, CryptographyException, ClientException, KeyStoreException, CertificateEncodingException, SignatureException, FileNotFoundException, NoSuchAlgorithmException, UnrecoverableEntryException {
        String username = "rksavinx";
        String password = "savinorules";
        URL fullUrl = new URL("https://10.1.71.134:8181/mtwilson/v1/");
        //URL fullUrl = new URL("http://localhost:8080/v1");
        String[] roles = { Role.Whitelist.toString(),Role.Attestation.toString(),Role.Security.toString()};
        
        ByteArrayResource certResource = new ByteArrayResource();
        SimpleKeystore keystore = KeystoreUtil.createUserInResource(certResource, username, password, fullUrl, roles);
        
        System.out.println("DONE");
    }
    
}
