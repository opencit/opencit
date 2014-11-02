/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.mtwilson.codec.Base64Util;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class KeyPairPemTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testReadKeyPairPemFile() throws Exception {
        // first create a key pair and certificate
        KeyPair cakey = RsaUtil.generateRsaKeyPair(1024);
        X509Builder builder = X509Builder.factory();
        X509Certificate cacert = builder.selfSigned("CN=test", cakey).build();
        String privateKeyPemContent = RsaUtil.encodePemPrivateKey(cakey.getPrivate());
        String cacertPemContent = X509Util.encodePemCertificate(cacert);
        String combined = privateKeyPemContent + cacertPemContent; // each already has a new line at the end
        log.debug("combined: {}", combined);
        // now try to read each one out of the combined file
        PrivateKey privateKey = RsaUtil.decodePemPrivateKey(combined);
        X509Certificate cert = X509Util.decodePemCertificate(combined);
        assertNotNull(privateKey);
        assertNotNull(cert);
    }
    
    @Test
    public void testGenerateKeyPair() throws Exception {
        KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
        String publicKey = Base64.encodeBase64String(keypair.getPublic().getEncoded());
        log.debug("public key: {}", publicKey);
        
    }
}
