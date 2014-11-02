/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import java.io.File;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class SimpleKeystoreTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimpleKeystoreTest.class);

    private File keystoreFile = new File("target" + File.separator + "test-keystore.jks");
    private String password = "password";
    
    @Test
    public void addPrivateKeyWithChain() throws Exception {
        
        // create the ca key
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048); // throws NoSuchAlgorithmException
        X509Builder cabuilder = X509Builder.factory();
        X509Certificate cacert = cabuilder.selfSigned("CN=ca", cakey).build();
        
        // create the subject key
        KeyPair subjectkey = RsaUtil.generateRsaKeyPair(2048);
        X509Builder builder = X509Builder.factory();
//        builder.selfSigned(tlsDistinguishedName, tlskey);
        builder.issuerName(cacert);
        builder.issuerPrivateKey(cakey.getPrivate());
        builder.subjectName("CN=subject");
        builder.subjectPublicKey(subjectkey.getPublic());
        X509Certificate subjectcert = builder.build();
        log.debug("subejct cert is null??? ?  {}", subjectcert);
        if( subjectcert == null  ) {
            for(Fault fault : builder.getFaults()) {
                log.debug("error: {}", fault.toString());
            }
        }
        // create the keystore
        SimpleKeystore keystore = new SimpleKeystore(keystoreFile, password);
        keystore.addKeyPairX509(subjectkey.getPrivate(), subjectcert, "subject", password, cacert);
        keystore.save();
    }
    
    @Test
    public void retrieveKey() throws Exception {
        SimpleKeystore keystore = new SimpleKeystore(keystoreFile, password);
        RsaCredentialX509 credential = keystore.getRsaCredentialX509("subject", password);
    }
}
