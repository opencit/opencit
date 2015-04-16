/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.Aes128;
import com.intel.dcsg.cpg.crypto.file.PemKeyEncryption;
import com.intel.dcsg.cpg.crypto.file.RsaPublicKeyProtectedPemKeyEnvelopeFactory;
import com.intel.dcsg.cpg.crypto.file.RsaPublicKeyProtectedPemKeyEnvelopeOpener;
import com.intel.dcsg.cpg.x509.X509Builder;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.crypto.SecretKey;
import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author jbuhacoff
 */
public class RsaKeyEnvelopeTest {
    @Test
    public void testRsaKeyEnvelopeExample() throws NoSuchAlgorithmException, CryptographyException, CertificateEncodingException {
        // create the rsa credential that will be used to seal & unseal the envelope
        KeyPair keyPair = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate certificate = X509Builder.factory().keyUsageKeyEncipherment().selfSigned("CN=test", keyPair).build();
        RsaCredentialX509 rsa = new RsaCredentialX509(keyPair.getPrivate(), certificate);
        // create the AES key that will be wrapped
        SecretKey secretKey = Aes128.generateKey();
        // seal the secret key
        RsaPublicKeyProtectedPemKeyEnvelopeFactory factory = new RsaPublicKeyProtectedPemKeyEnvelopeFactory(certificate);
        PemKeyEncryption envelope = factory.seal(secretKey);
        // unseal the secret key
        RsaPublicKeyProtectedPemKeyEnvelopeOpener recipient = new RsaPublicKeyProtectedPemKeyEnvelopeOpener(rsa);
        Key unwrappedKey = recipient.unseal(envelope);
        // check that we got the same key back
        assertEquals(secretKey.getAlgorithm(), unwrappedKey.getAlgorithm());
        assertTrue(Arrays.equals(secretKey.getEncoded(), unwrappedKey.getEncoded()));
    }
    
    @Test(expected=CryptographyException.class)
    public void testRsaKeyEnvelopeWithWrongPrivateKey() throws NoSuchAlgorithmException, CryptographyException, CertificateEncodingException {
        // create the rsa credential that will be used to seal the envelope
        KeyPair keyPair1 = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate certificate1 = X509Builder.factory().keyUsageKeyEncipherment().selfSigned("CN=test1", keyPair1).build();
        RsaCredentialX509 rsa1 = new RsaCredentialX509(keyPair1.getPrivate(), certificate1);
        // create the other rsa credential that will be used (unsuccessfully) to unseal the envelope
        KeyPair keyPair2 = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate certificate2 = X509Builder.factory().keyUsageKeyEncipherment().selfSigned("CN=test2", keyPair2).build();
        RsaCredentialX509 rsa2 = new RsaCredentialX509(keyPair2.getPrivate(), certificate2);
        // create the AES key that will be wrapped
        SecretKey secretKey = Aes128.generateKey();
        // seal the secret key
        RsaPublicKeyProtectedPemKeyEnvelopeFactory factory = new RsaPublicKeyProtectedPemKeyEnvelopeFactory(certificate1);
        PemKeyEncryption envelope = factory.seal(secretKey);
        // try to unseal the secret key with the wrong private key
        RsaPublicKeyProtectedPemKeyEnvelopeOpener recipient = new RsaPublicKeyProtectedPemKeyEnvelopeOpener(rsa2);
        Key unwrappedKey = recipient.unseal(envelope); // throws CryptographyException: IllegalArgumentException: RsaKeyEnvelope created with md5-hash-of-rsa1-certificate cannot be unsealed with private key corresponding to md5-hash-of-rsa2-certificate
    }
}
