/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.io.ExistingFileResource;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.x509.repository.PemMutableCertificateRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class TestPemFile {
    File tmp = new File(System.getProperty("user.home", ".")+File.separator+"tls-policy.test.pem");
    
    public static KeyPair generateRsaKeyPair(int keySizeInBits) throws NoSuchAlgorithmException {
        KeyPairGenerator r = KeyPairGenerator.getInstance("RSA");
        r.initialize(keySizeInBits);
        KeyPair keypair = r.generateKeyPair();
        return keypair;
    }
    
    @Test
    public void testWritePemFile() throws NoSuchAlgorithmException, IOException, CertificateException, KeyManagementException {
        KeyPair keypair1 = generateRsaKeyPair(1024);
        X509Certificate c1 = new X509Builder().selfSigned("CN=test1,C=US", keypair1).expires(365, TimeUnit.DAYS).randomSerial().build();
        KeyPair keypair2 = generateRsaKeyPair(1024);
        X509Certificate c2 = new X509Builder().selfSigned("CN=test2,C=US", keypair2).expires(365, TimeUnit.DAYS).randomSerial().build();
        PemMutableCertificateRepository repository = new PemMutableCertificateRepository(new FileResource(tmp));
        repository.addCertificate(c1);
        repository.addCertificate(c2);
    }
    
    @Test
    public void testReadPemFile() throws FileNotFoundException, IOException, CertificateException {
        InputStream in = new FileInputStream(tmp);
        String pemContent = IOUtils.toString(in);
        System.out.println(pemContent);   
        System.out.println("Certificates:");
        PemMutableCertificateRepository repository = new PemMutableCertificateRepository(new ExistingFileResource(tmp));        
        for(X509Certificate certificate : repository.getCertificates()) {
            System.out.println(certificate.getSubjectX500Principal().getName());
        }
    }
}
