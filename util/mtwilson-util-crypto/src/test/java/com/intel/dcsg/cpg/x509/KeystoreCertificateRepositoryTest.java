/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.x509.repository.KeystoreCertificateRepository;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class KeystoreCertificateRepositoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeystoreCertificateRepositoryTest.class);
    
    @Test
    public void testKeystoreCertificateRepository() throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException, KeyManagementException {
        String password = "password";
        KeyPair key1 = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate certificate1 = X509Builder.factory().selfSigned("CN=tes1t", key1).build();
        KeyPair key2 = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate certificate2 = X509Builder.factory().selfSigned("CN=test2", key2).build();
        // create a new keystore
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType()); // throws KeyStoreException
        keystore.load(null, password.toCharArray());
        keystore.setCertificateEntry("test1", certificate1);
        // create a certificate repository
        KeystoreCertificateRepository repository = new KeystoreCertificateRepository(keystore, password);
        List<X509Certificate> list1 = repository.getCertificates();
        log.debug("There are {} certificates in the keystore certificate repository", list1.size());
        for(X509Certificate item : list1) {
            log.debug("Certificate: {}", item.getSubjectX500Principal().getName());
        }
        // add the second key using the repository interface
        repository.addCertificate(certificate2);
       List<X509Certificate> list2 = repository.getCertificates();
        log.debug("There are {} certificates in the keystore certificate repository", list2.size());
        for(X509Certificate item : list2) {
            log.debug("Certificate: {}", item.getSubjectX500Principal().getName());
        }
         
    }
}
