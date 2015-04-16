/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.crypto.Aes128;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.file.PasswordProtectedKeyPemEnvelopeFactory;
import com.intel.dcsg.cpg.crypto.file.PasswordProtectedKeyPemEnvelopeOpener;
import com.intel.dcsg.cpg.crypto.file.PemKeyEncryption;
import com.intel.dcsg.cpg.crypto.file.PemKeyEncryptionUtil;
import com.intel.dcsg.cpg.io.pem.Pem;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.SecretKey;
import org.apache.commons.codec.binary.Base64;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class PasswordKeyEnvelopeTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private String generatePassword(int length) {
        Random rnd = new Random();
        String password = "";
        for(int i=0; i<length; i++) {
            password += String.valueOf((char)('a'+rnd.nextInt(26)));
        }
        return password;
    }
    
    /**
     * Sample output:
     * 
Password: fovddbft
-----BEGIN SECRET KEY-----
EnvelopeKeyId: bkaz4lbv/y0=:etpZPPlmwEilwbNxzBHRCd+FyWQMw1akygtgIPent6w=
EnvelopeAlgorithm: PBEWithMD5AndDES
ContentAlgorithm: AES

BVXEednbBCISCi85E21ORa0cZsLnLfcU
-----END SECRET KEY-----
     * @throws CryptographyException 
     */
    @Test
    public void testWritePasswordKeyEnvelopeFormat() throws CryptographyException {
        String password = generatePassword(8);
        SecretKey secretKey = Aes128.generateKey();
        PasswordProtectedKeyPemEnvelopeFactory factory = new PasswordProtectedKeyPemEnvelopeFactory(password);
        PemKeyEncryption envelope = factory.seal(secretKey);
        String output = envelope.toString();
        System.out.println("Password: "+password);
        System.out.println(output);
    }
    
    @Test
    public void testReadPasswordKeyEnvelopeFormat() throws CryptographyException {
        String pem = "" +
"-----BEGIN SECRET KEY-----\n" +
"EnvelopeKeyId: bkaz4lbv/y0=:etpZPPlmwEilwbNxzBHRCd+FyWQMw1akygtgIPent6w=\n" +
"EnvelopeAlgorithm: PBEWithMD5AndDES\n" +
"ContentAlgorithm: AES\n" +
"\n" +
"BVXEednbBCISCi85E21ORa0cZsLnLfcU\n" +
"-----END SECRET KEY-----\n";
        PemKeyEncryption envelope = PemKeyEncryptionUtil.getEnvelope(Pem.valueOf(pem));
        assertEquals("bkaz4lbv/y0=:etpZPPlmwEilwbNxzBHRCd+FyWQMw1akygtgIPent6w=", envelope.getEncryptionKeyId());
        assertEquals("PBEWithMD5AndDES", envelope.getEncryptionAlgorithm());
        assertEquals("AES", envelope.getContentAlgorithm());
        assertEquals("BVXEednbBCISCi85E21ORa0cZsLnLfcU", Base64.encodeBase64String(envelope.getDocument().getContent()));
    }
    
    @Test
    public void testPasswordKeyEnvelopeExample() throws NoSuchAlgorithmException, CryptographyException, CertificateEncodingException {
        // create the password that will be used to seal the envelope
        String password = generatePassword(8);
        // create the AES key that will be wrapped
        SecretKey secretKey = Aes128.generateKey();
        // seal the secret key
        PasswordProtectedKeyPemEnvelopeFactory factory = new PasswordProtectedKeyPemEnvelopeFactory(password);
        PemKeyEncryption envelope = factory.seal(secretKey);
        log.debug("content algorithm: {}", envelope.getContentAlgorithm());
        log.debug("envelope algorithm: {}", envelope.getEncryptionAlgorithm());
        log.debug("envelope key id: {}", envelope.getEncryptionKeyId());
        log.debug("envelope pem:\n{}\n", envelope.getDocument().toString());
        // unseal the secret key
        PasswordProtectedKeyPemEnvelopeOpener recipient = new PasswordProtectedKeyPemEnvelopeOpener(password);
        Key unwrappedKey = recipient.unseal(envelope);
        // check that we got the same key back
        assertEquals(secretKey.getAlgorithm(), unwrappedKey.getAlgorithm());
        assertTrue(Arrays.equals(secretKey.getEncoded(), unwrappedKey.getEncoded()));
    }
    
    @Test(expected=CryptographyException.class)
    public void testPasswordKeyEnvelopeWithWrongPassword() throws NoSuchAlgorithmException, CryptographyException, CertificateEncodingException {
        // create the password that will be used to seal the envelope
        String password1 = generatePassword(8);
        // create the other password that will be used (unsuccessfully) to unseal the envelope
        String password2 = generatePassword(8);
        // create the AES key that will be wrapped
        SecretKey secretKey = Aes128.generateKey();
        // seal the secret key
        PasswordProtectedKeyPemEnvelopeFactory factory = new PasswordProtectedKeyPemEnvelopeFactory(password1);
        PemKeyEncryption envelope = factory.seal(secretKey);
        // try to unseal the secret key with the wrong password
        PasswordProtectedKeyPemEnvelopeOpener recipient = new PasswordProtectedKeyPemEnvelopeOpener(password2);
        Key unwrappedKey = recipient.unseal(envelope); // throws CryptographyException: IllegalArgumentException: PasswordKeyEnvelope created with salted-hash-of-password1 cannot be unsealed with private key corresponding to salted-hash-of-password2
    }
}
