/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.*;
import com.intel.dcsg.cpg.configuration.CommonsConfigurationUtil;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.mtwilson.datatypes.*;
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
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These "unit tests" are intended to help a developer setup the local environment
 * for testing the API. The code here parallels what must be done during installation.
 * @author jbuhacoff
 */
public class Bootstrap {
    public static Logger log = LoggerFactory.getLogger(Bootstrap.class);
    public static Configuration config;
    
    @BeforeClass
    public static void configure() throws IOException {
        System.setProperty("com.intel.level", "FINEST");
        log = LoggerFactory.getLogger(Bootstrap.class);
        
        String env = "/mtwilson-0.5.2.properties";   // choose your environment (properties file under src/test/env)
//        String env = "/localhost-0.5.2.properties";   // choose your environment (properties file under src/test/env)
        config = CommonsConfigurationUtil.fromResource(env);
        log.debug("mtwilson.api.baseurl={}",config.getString("mtwilson.api.baseurl"));
        log.debug("mtwilson.api.keystore={}",config.getString("mtwilson.api.keystore"));
    }
    
    /*
     * Use the SimpleKeystore class to create or open the configured keystore file,
     * connect to the configured mtwilson.api.baseurl and save the server's SSL
     * certificate to the keystore file.
     */
    @Test
    public void addServerSslCertUsingSimpleKeystore() throws KeyManagementException, KeyStoreException, MalformedURLException, IOException, Exception {
        // create new keystore (if file does not exist and is writable it will be created when we save)
        SimpleKeystore keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
        printKeystoreContents(keystore); // list certificates
        addServerSslCertificateToKeystore(new URL(config.getString("mtwilson.api.baseurl")), keystore, config.getString("mtwilson.api.ssl.protocol", "TLS")); // add server ssl certificate
        printKeystoreContents(keystore); // list certificates again
        keystore.save(); // commit chanages to disk
    }

    /**
     * Should be done AFTER addServerSslCertUsingSimpleKeystore
     * @throws Exception 
     */
    @Test
    public void addNewApiClientKeypairUsingSimpleKeystore() throws KeyManagementException, Exception {
        // create new keystore (if file does not exist and is writable it will be created when we save)
        SimpleKeystore keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
        createNewApiClientKeypair(keystore);
        keystore.save(); // save the new generated keypair
        registerNewApiClientKeypair(keystore);
        selfApproveNewApiClientKeypair(keystore);
    }

    /**
     * Should be done AFTER addServerSslCertUsingSimpleKeystore, and is an alternative to addNewApiClientKeypairUsingSimpleKeystore
     * @throws Exception 
     */
    @Test
    public void registerExistingApiClient() throws KeyManagementException, Exception {
        // create new keystore (if file does not exist and is writable it will be created when we save)
        SimpleKeystore keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
        registerNewApiClientKeypair(keystore);
        selfApproveNewApiClientKeypair(keystore);
    }
    
    /**
     * Should be done AFTER addNewApiClientKeypairUsingSimpleKeystore
     * 
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws MalformedURLException
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws UnrecoverableEntryException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Test
    public void testGetSamlCertificate() throws NoSuchAlgorithmException, KeyManagementException, MalformedURLException, KeyStoreException, IOException, CertificateException, UnrecoverableEntryException, ApiException, SignatureException, ClientException {
        ApiClient api = new ApiClient(config);
        X509Certificate certificate = api.getSamlCertificate();
        log.debug("SAML Certificate Subject: {}", certificate.getSubjectX500Principal().getName());
        log.debug("SAML Certificate Issuer: {}", certificate.getIssuerX500Principal().getName());
        URL attestationService = new URL(config.getString("mtwilson.api.baseurl"));
        SimpleKeystore keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
        keystore.addTrustedSamlCertificate(certificate, attestationService.getHost());
        keystore.save();
        log.info("Saved SAML certificate in keystore");
    }
    
    
    /* 
     * Performs an arbitrary api call, "listAllOS"...
     * Fails if the keystore does not have the server's SSL certificate
     */
    @Test
    public void executeApiCall() throws ClientException, IOException, IOException, ApiException, SignatureException {
        // test an API call to see if things are working.
        ApiClient c = new ApiClient(config);
        List<OsData> list = c.listAllOS();
        System.out.println(String.format("Got list size %d", list.size()));                    
    }

    private void printKeystoreContents(SimpleKeystore keystore) throws KeyStoreException {
        System.out.println("Keystore contents:");
        String[] aliases1 = keystore.listTrustedSslCertificates();
        for(String alias : aliases1) { System.out.println("Certificate: "+alias); }
        System.out.println(String.format("%d certificates", aliases1.length));        
    }
    
    /**
     * Uses the server's address as the certificate alias
     * then the policy implementation will add it to the keystore IF it's not already there,
     * otherwise and also if it's "trust known" it will check if it's there and if so it will
     * use it, but it will never replace it with a new one or accept anything else - 
     * and if it's a trust CA policy nothing needs to be added.
     * @param server
     * @param keystore
     * @param tlsProtocol for example SSL, SSLv2, SSLv3, TLS, TLSv1.1, TLSv1.2
     * @throws Exception 
     */
    private void addServerSslCertificateToKeystore(URL server, SimpleKeystore keystore, String tlsProtocol) throws NoSuchAlgorithmException, KeyManagementException, KeyManagementException, IOException, CertificateEncodingException, CertificateEncodingException {
        X509Certificate[] certs = TlsUtil.getServerCertificates(server, tlsProtocol);
        String aliasBasename = server.getHost();
        if( certs.length == 1 ) {
            System.out.println("Adding trusted certificate with SHA-1 fingerprint: "+Hex.encodeHexString(sha1fingerprint(certs[0])));
            keystore.addTrustedSslCertificate(certs[0], aliasBasename);     
        }
        if( certs.length > 1 ) {
            int certificateNumber = 0;
            for(X509Certificate cert : certs) {
                certificateNumber++;
                String alias = String.format("%s-%d", aliasBasename, certificateNumber);
                keystore.addTrustedSslCertificate(cert, alias);
                System.out.println("Added certificate to keystore: "+alias+" with fingerprint: "+Hex.encodeHexString(sha1fingerprint(cert)));
            }        
        }
    }
    
    public static byte[] sha1fingerprint(X509Certificate certificate) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest hash = MessageDigest.getInstance("SHA-1"); // NoSuchAlgorithmException
        byte[] digest = hash.digest(certificate.getEncoded()); // CertificateEncodingException
        return digest;
    }
    
    
    private void createNewApiClientKeypair(SimpleKeystore keystore) throws KeyStoreException, NoSuchAlgorithmException, CryptographyException, IOException, KeyManagementException {
        // create a new private key and certificate only if it does not already exist in the keystore
        String[] aliases = keystore.aliases();
        if( ArrayUtils.contains(aliases, config.getString("mtwilson.api.key.alias")) ) {
            System.out.println("Keystore already contains key with alias: "+config.getString("mtwilson.api.key.alias"));
            return;
        }
        KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        X509Certificate certificate = RsaUtil.generateX509Certificate("jonathan"/*CN=jonathan, OU=IASI, O=Intel, L=Folsom, ST=CA, C=US"*/, keypair, RsaUtil.DEFAULT_RSA_KEY_EXPIRES_DAYS);
        //RsaCredentialX509 credential = new RsaCredentialX509(keypair.getPrivate(), certificate);
        keystore.addKeyPairX509(keypair.getPrivate(), certificate, config.getString("mtwilson.api.key.alias"), config.getString("mtwilson.api.key.password"));
    }
    
        // register the new key with Mt Wilson (should be done AFTER adding the ssl certs to keystore)
    private void registerNewApiClientKeypair(SimpleKeystore keystore) throws FileNotFoundException, KeyStoreException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, UnrecoverableEntryException, CertificateEncodingException, ClientException, IOException, ApiException, SignatureException, com.intel.dcsg.cpg.crypto.CryptographyException {
        RsaCredentialX509 rsaCredential = keystore.getRsaCredentialX509(config.getString("mtwilson.api.key.alias"), config.getString("mtwilson.api.key.password"));
        ApiClient c = new ApiClient(config);
        ApiClientCreateRequest me = new ApiClientCreateRequest();
        me.setCertificate(rsaCredential.getCertificate().getEncoded());
        me.setRoles(new String[] { Role.Attestation.toString(), Role.Whitelist.toString() });
        c.register(me);
        
        /*
        Properties p = new Properties();
        p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "false");
        p.setProperty("mtwilson.api.ssl.verifyHostname", "false");
        ApiClient c = new ApiClient(new URL(config.getString("mtwilson.api.baseurl")), rsaCredential, p);
        */
    }
    
    /**
     * Call this after registerNewApiClientKeypair,  and server must have 
     * your current IP in the trusted ip list.
     * @param keystore
     * @throws Exception 
     */
    private void selfApproveNewApiClientKeypair(SimpleKeystore keystore) throws FileNotFoundException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException, ClientException, IOException, ApiException, SignatureException, com.intel.dcsg.cpg.crypto.CryptographyException {
        RsaCredentialX509 rsaCredential = keystore.getRsaCredentialX509(config.getString("mtwilson.api.key.alias"), config.getString("mtwilson.api.key.password"));
        ApiClient c = new ApiClient(config); // KeyManagementException, [MalformedURLException], [UnsupportedEncodingException]
        ApiClientUpdateRequest update = new ApiClientUpdateRequest();
        update.fingerprint = rsaCredential.identity();
        update.enabled = true;
        update.roles = new String[] { Role.Attestation.toString(), Role.Whitelist.toString() };
        update.status = ApiClientStatus.APPROVED.toString();
        update.comment = "Bootstrap approval sample code in JavaIntegrationTests project";
        c.updateApiClient(update); // ApiException, SignatureException
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
    /*
    public static void main(String[] args) throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, UnsupportedEncodingException, IOException, ApiException, SignatureException, KeyStoreException, CertificateException {
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
                X509Certificate[] serverCertificates = RsaUtil.getServerCertificates(url);
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
        * 
    }
        */
    
}
