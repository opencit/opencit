/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x500.DN;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.*;
import org.junit.Test;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.X500Name;

/**
 *
 * @author jbuhacoff
 */
public class SubjectAlternativeNameTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SubjectAlternativeNameTest.class);
    
    @Test
    public void testExtractSubjectAlternativeName() throws NoSuchAlgorithmException, GeneralSecurityException, IOException, CryptographyException {
        KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate rsa = RsaUtil.generateX509Certificate("Test Cert", "ip:1.2.3.4", keypair, 30);
        String alternativeName = X509Util.ipAddressAlternativeName(rsa);
        log.debug("alternativeName: {}", alternativeName);
    }
    
    @Test
    public void testExtractSubjectAlternativeNameNull() throws NoSuchAlgorithmException, GeneralSecurityException, IOException, CryptographyException {
        KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate rsa = RsaUtil.generateX509Certificate("Test Cert", keypair, 30);
        String alternativeName = X509Util.ipAddressAlternativeName(rsa);
        log.debug("alternativeName: {}", alternativeName); // should be null
    }
    
    @Test
    public void compareOriginalX509FactoryMethodToX509CertificateBuilder() throws Exception {
        KeyPair caKeys = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate caCert = RsaUtil.generateX509Certificate("Test CA Cert", caKeys, 30);
        KeyPair subjectKeys = RsaUtil.generateRsaKeyPair(1024);
        // Using the original factory method:  (has limitation on distinguished name, you only provide the value for CN and it automatically adds CN= and also the others)
        CertificateIssuerName issuerName = new CertificateIssuerName(X500Name.asX500Name(caCert.getSubjectX500Principal()));
        X509Certificate subjectCert1 = RsaUtil.createX509CertificateWithIssuer(subjectKeys.getPublic(), "test1", "ip:1.2.3.4", 30/*days*/, caKeys.getPrivate(), issuerName);
        writeCert(subjectCert1, "test1.crt");
        // Using the new X509Builder method: (does not limit subject name)
        X509Builder x509 = new X509Builder();
        x509.subjectName("CN=test1").alternativeName("ip:1.2.3.4").expires(30, TimeUnit.DAYS).subjectPublicKey(subjectKeys.getPublic()).issuerPrivateKey(caKeys.getPrivate()).issuerName(caCert); // works, results in equivalent certificate
        X509Certificate subjectCert2 = x509.build();
        writeCert(subjectCert2, "test2.crt");
    }
    
    @Test
    public void testUsingRsaCredentialX509() throws Exception {
        KeyPair caKeys = RsaUtil.generateRsaKeyPair(1024);
        X509Builder caBuilder = X509Builder.factory().subjectName("CN=Test CA Cert").alternativeName("dns:server.com").expires(30, TimeUnit.DAYS).subjectPublicKey(caKeys.getPublic()).issuerPrivateKey(caKeys.getPrivate()).issuerName("CN=Test CA Cert");
        X509Certificate caCert = caBuilder.build();
        writeCert(caCert, "testca.crt");
        RsaCredentialX509 ca = new RsaCredentialX509(caKeys.getPrivate(), caCert);
        KeyPair subjectKeys = RsaUtil.generateRsaKeyPair(1024);
        X509Builder subjectBuilder = X509Builder.factory().subjectName("CN=test1").alternativeName("ip:1.2.3.4").expires(30, TimeUnit.DAYS).subjectPublicKey(subjectKeys.getPublic()).issuer(ca);
        X509Certificate subjectCert = subjectBuilder.build();
        writeCert(subjectCert, "test.crt");
    }

    @Test
    public void testUsingRsaCredentialX509AndSelfSigned() throws NoSuchAlgorithmException, FileNotFoundException, CertificateEncodingException, IOException, CryptographyException  {
        KeyPair caKeys = RsaUtil.generateRsaKeyPair(1024);
        X509Builder caBuilder = X509Builder.factory().selfSigned("CN=Test CA Cert", caKeys).alternativeName("dns:server.com").expires(30, TimeUnit.DAYS);
        X509Certificate caCert = caBuilder.build();
        writeCert(caCert, "testca.crt");
        RsaCredentialX509 ca = new RsaCredentialX509(caKeys.getPrivate(), caCert);
        KeyPair subjectKeys = RsaUtil.generateRsaKeyPair(1024);
        X509Builder subjectBuilder = X509Builder.factory().subjectName("CN=test1").alternativeName("ip:1.2.3.4").expires(30, TimeUnit.DAYS).subjectPublicKey(subjectKeys.getPublic()).issuer(ca);
        X509Certificate subjectCert = subjectBuilder.build();
        writeCert(subjectCert, "test.crt");
    }
    
    @Test
    public void testPemEncode() throws Exception {
        KeyPair caKeys = RsaUtil.generateRsaKeyPair(1024);
        X509Builder caBuilder = X509Builder.factory().selfSigned("CN=Test CA Cert", caKeys).alternativeName("dns:server.com").expires(30, TimeUnit.DAYS);
        X509Certificate caCert = caBuilder.build();
        writeCert(caCert, "testca.crt");
        String pem = X509Util.encodePemCertificate(caCert);
        writeString(pem, "testca1.pem");
    }
    
    /**
     * 
     * @param relativeFileName  for example, "test1.crt"  it is relative to your home directory
     */
    private void writeCert(X509Certificate certificate, String relativeFileName) throws FileNotFoundException, CertificateEncodingException, IOException {
        String filename = System.getProperty("user.home")+File.separator+relativeFileName;
        FileOutputStream out = new FileOutputStream(new File(filename));
        IOUtils.copy(new ByteArrayInputStream(certificate.getEncoded()), out);
        out.close();
        log.debug("writeCert filename: {}", filename);        
    }

    /**
     * 
     * @param relativeFileName  for example, "test1.crt"  it is relative to your home directory
     */
    private void writeString(String text, String relativeFileName) throws FileNotFoundException, CertificateEncodingException, IOException {
        String filename = System.getProperty("user.home")+File.separator+relativeFileName;
        FileOutputStream out = new FileOutputStream(new File(filename));
        IOUtils.copy(new ByteArrayInputStream(text.getBytes("UTF-8")), out);
        out.close();
        log.debug("writeString filename: {}",filename);        
    }
    
    @Test
    public void testCommonNameFromLdapName() {
        DN dn = new DN("CN=abc,OU=def,O=ghi,C=US");
        log.debug("CN1: {}", dn.get("CN")); 
        assertEquals("abc", dn.get("CN"));
        DN dn2 = new DN("CN=ABC,CN=abc,OU=def,O=ghi,C=US");
        log.debug("CN2: {}", dn2.get("CN"));
        assertEquals("ABC", dn2.get("CN")); // retrieves only the first one
    }
    
    
    @Test
    public void testX509Builder() throws FileNotFoundException, CertificateEncodingException, IOException, NoSuchAlgorithmException {
        KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
        X509Builder x509 = new X509Builder();

        
//        X509Certificate cert = x509.commonName("jonathan").country("US").issuerPrivateKey(keypair.getPrivate()).subjectPublicKey(keypair.getPublic()).build();
//        error: commonName(jonathan) organizationUnit(null) organizationName(null) country(US) [java.lang.NullPointerException]
//        error: cannot sign certificate [java.lang.NullPointerException]

//        X509Certificate cert = x509.commonName("jonathan").organizationName("Intel").organizationUnit("Mt Wilson").country("US").issuerPrivateKey(keypair.getPrivate()).subjectPublicKey(keypair.getPublic()).build();
//        works
        
//        X509Certificate cert = x509.subjectName("CN=jonathan").issuerPrivateKey(keypair.getPrivate()).subjectPublicKey(keypair.getPublic()).build();
//        error: cannot sign certificate [java.lang.NullPointerException]   because missing issuerName()

//        X509Certificate cert = x509.subjectName("CN=jonathan").issuerName("CN=jonathan").issuerPrivateKey(keypair.getPrivate()).subjectPublicKey(keypair.getPublic()).build();
//        works

        X509Certificate cert = x509.subjectName("CN=jonathan")
                                    .issuerName("CN=jonathan")
                                    .issuerPrivateKey(keypair.getPrivate())
                                    .subjectPublicKey(keypair.getPublic())
//                                    .ipAlternativeName("1.2.3.4") // works
//                                    .ipAlternativeName("ip:1.2.3.4") // works
//                                    .dnsAlternativeName("server.com") // works
//                                    .dnsAlternativeName("dns:server.com") // works
//                                    .dnsAlternativeName("*.server.com") // does NOT work, you would have to put it in the CN
//                                    .ipAlternativeName("1.2.3.4").dnsAlternativeName("server.com") // works (both show up on certificate)
//                                    .keyUsageCertificateAuthority() // works
//                                    .keyUsageDigitalSignature() // works
//                                      .keyUsageDataEncipherment() // works
//                                    .keyUsageNonRepudiation()// works
//                                    .keyUsageKeyEncipherment()// works
//                                    .keyUsageCRLSign() // works
                                    .build();
//        works
        
        log.debug("builder isValid() = {}",x509.isValid()); 
        for(Fault f :  x509.getFaults()) {
            log.error(String.format("%s: %s", f.getClass().getName(), f.toString()));
        }
        if( cert == null ) { 
            log.error("certificate is null"); 
        }
        else {
            String outFilename = System.getProperty("user.home")+File.separator+"test.crt";
            FileOutputStream out = new FileOutputStream(new File(outFilename));
            IOUtils.copy(new ByteArrayInputStream(cert.getEncoded()), out);
            out.close();
            log.error("success, saved certificate in {}",outFilename);
        }
    }
    
    
    @Test
    public void testBuildSelfSignedTlsCertificateInKeystoreForPrivacyCa() throws FileNotFoundException, CertificateEncodingException, IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, CertificateException {
        KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
        X509Builder x509 = new X509Builder();
        X509Certificate cert = x509.subjectName("CN=privacyca.mtwilson.local")   // in order to use it, you need to define the ip address of privacyca.mtwilson.local in /etc/hosts
                                    .issuerName("CN=privacyca.mtwilson.local")
                                    .issuerPrivateKey(keypair.getPrivate())
                                    .subjectPublicKey(keypair.getPublic())
                                    // privacy ca client code requires an alternative name:
                                    .dnsAlternativeName("privacyca.mtwilson.local")
                                    // these 3 are what TLS certificates do:
                                    .keyUsageDigitalSignature()
                                    .keyUsageDataEncipherment()
                                    .keyUsageKeyEncipherment()
                                    .build();
        // assuming glassfish with default key alias "s1as" and default password "changeit"
        String outFilename = System.getProperty("user.home")+File.separator+"privacyca.jks";
        FileResource keystoreFile = new FileResource(new File(outFilename));
        SimpleKeystore keystore = new SimpleKeystore(keystoreFile, "changeit"); 
        keystore.addKeyPairX509(keypair.getPrivate(), cert, "s1as", "changeit");
        keystore.save();
        log.debug("success, saved certificate in {}", outFilename);
    }    
}
