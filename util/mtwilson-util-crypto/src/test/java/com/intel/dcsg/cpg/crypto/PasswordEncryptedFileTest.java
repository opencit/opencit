/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class PasswordEncryptedFileTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
-----BEGIN ENCRYPTED DATA-----
Content-Encoding: base64
Encryption-Algorithm: PBEWithSHA1AndDESede/CBC/PKCS5Padding
Encryption-Key-Id: x/Q2ZCId0qo=:n2hs1YJ3NPpTvABtRbJLqBq6PKM5U7LfMEKKT1F4eME=
Integrity-Algorithm: SHA256
Key-Algorithm: PBEWithSHA1AndDESede; iterations=1048576; key-length=168; salt-bytes=8

N8kemROReNZBtAggOnhYLL8GLjsLLWAnmuCrF+S18EaUCno+qxJryvhEG00H9l7O59/s6kwy0h4=
-----END ENCRYPTED DATA-----
     * 
     * @throws IOException 
     */
    @Test
    public void testEncryptTextMtWilson_1_2() throws IOException {
        PasswordProtection protection = PasswordProtectionBuilder.factory().digestAlgorithm("SHA-256").keyAlgorithm("PBEWithSHA1AndDESede").mode("CBC").padding("PKCS5Padding").build();
        log.debug("algorithm {}", protection.getAlgorithm());
        log.debug("cipher {}", protection.getCipher());
//        String content = "hello world";
        String content = "foo=bar\nbaz=quuzx\n";
        String password = "password";
        ByteArrayResource resource = new ByteArrayResource();
        PasswordEncryptedFile enc = new PasswordEncryptedFile(resource, password, protection);
        enc.saveString(content);
        
        String encryptedContentEnvelope = new String(resource.toByteArray());
        log.debug("Input content: {}", content);
        log.debug("Encrypted file: {}", encryptedContentEnvelope);
        
        String decryptedContent = enc.loadString();
        log.debug("Decrypted content: {}", decryptedContent);
        assertEquals(content, decryptedContent);
    }
        
    @Test
    public void testDecryptTextMtWilson_1_2() throws Exception {
        String ciphertext = "-----BEGIN ENCRYPTED DATA-----\n" +
"Content-Encoding: base64\n" +
"Encryption-Algorithm: PBEWithSHA1AndDESede/CBC/PKCS5Padding\n" +
"Encryption-Key-Id: x/Q2ZCId0qo=:n2hs1YJ3NPpTvABtRbJLqBq6PKM5U7LfMEKKT1F4eME=\n" +
"Integrity-Algorithm: SHA256\n" +
"Key-Algorithm: PBEWithSHA1AndDESede; iterations=1048576; key-length=168; salt-bytes=8\n" +
"\n" +
"N8kemROReNZBtAggOnhYLL8GLjsLLWAnmuCrF+S18EaUCno+qxJryvhEG00H9l7O59/s6kwy0h4=\n" +
"-----END ENCRYPTED DATA-----";
        String password = "password";
        ByteArrayResource resource = new ByteArrayResource(ciphertext.getBytes());
        PasswordEncryptedFile enc = new PasswordEncryptedFile(resource, password);
        String content = enc.loadString();
        log.debug("decrypted: {}", content);
        assertEquals("hello world", content);
    }

    @Test
    public void testDecryptTextMtWilson_1_2_NoKeyAlgParams() throws Exception {
        String ciphertext = "-----BEGIN ENCRYPTED DATA-----\n" +
"Content-Encoding: base64\n" +
"Encryption-Algorithm: PBEWithSHA1AndDESede/CBC/PKCS5Padding\n" +
"Encryption-Key-Id: x/Q2ZCId0qo=:n2hs1YJ3NPpTvABtRbJLqBq6PKM5U7LfMEKKT1F4eME=\n" +
"Integrity-Algorithm: SHA256\n" +
"Key-Algorithm: PBEWithSHA1AndDESede\n" +
"\n" +
"N8kemROReNZBtAggOnhYLL8GLjsLLWAnmuCrF+S18EaUCno+qxJryvhEG00H9l7O59/s6kwy0h4=\n" +
"-----END ENCRYPTED DATA-----";
        String password = "password";
        ByteArrayResource resource = new ByteArrayResource(ciphertext.getBytes());
        PasswordEncryptedFile enc = new PasswordEncryptedFile(resource, password);
        String content = enc.loadString();
        log.debug("decrypted: {}", content);        
        assertEquals("hello world", content);
    }

    @Test
    public void testDecryptTextMtWilson_1_2_NoKeyAlg() throws Exception {
        String ciphertext = "-----BEGIN ENCRYPTED DATA-----\n" +
"Content-Encoding: base64\n" +
"Encryption-Algorithm: PBEWithSHA1AndDESede/CBC/PKCS5Padding\n" +
"Encryption-Key-Id: x/Q2ZCId0qo=:n2hs1YJ3NPpTvABtRbJLqBq6PKM5U7LfMEKKT1F4eME=\n" +
"Integrity-Algorithm: SHA256\n" +
"\n" +
"N8kemROReNZBtAggOnhYLL8GLjsLLWAnmuCrF+S18EaUCno+qxJryvhEG00H9l7O59/s6kwy0h4=\n" +
"-----END ENCRYPTED DATA-----";
        String password = "password";
        ByteArrayResource resource = new ByteArrayResource(ciphertext.getBytes());
        PasswordEncryptedFile enc = new PasswordEncryptedFile(resource, password);
        String content = enc.loadString();
        log.debug("decrypted: {}", content);        
        assertEquals("hello world", content);
    }
    
    /**
-----BEGIN ENCRYPTED DATA-----
Content-Encoding: base64
Encryption-Algorithm: AES/CBC/PKCS5Padding
Encryption-Key-Id: mhl78/TP4g4=:4bzsOCFP1BeY6unmsBDT0B0VxmW9fQO0bQh/6yZqgLY=
Integrity-Algorithm: SHA256
Key-Algorithm: PBKDF2WithHmacSHA1; iterations=1; key-length=128; salt-bytes=16

tYk3OHzKZ3hyG73Mm/nvuts0bsa+x+59G8VuDGCEED8cyjegedwzB1pnL/bjkfUWoSlU71DuU43J
Lu3ibINeRux4Nql10rXNW6LpJpg3Kdo=
-----END ENCRYPTED DATA-----
     * 
     * @throws IOException 
     */
    @Test
    public void testEncryptText() throws IOException {
        PasswordProtection protection = PasswordProtectionBuilder.factory().aes(128).digestAlgorithm("SHA-256").keyAlgorithm("PBKDF2WithHmacSHA1").mode("CBC").padding("PKCS5Padding").build();
        log.debug("algorithm {}", protection.getAlgorithm());
        log.debug("cipher {}", protection.getCipher());
        String content = "hello world";
        String password = "password";
        ByteArrayResource resource = new ByteArrayResource();
        PasswordEncryptedFile enc = new PasswordEncryptedFile(resource, password, protection);
        enc.saveString(content);
        
        String encryptedContentEnvelope = new String(resource.toByteArray());
        log.debug("Input content: {}", content);
        log.debug("Encrypted file: {}", encryptedContentEnvelope);
        
        String decryptedContent = enc.loadString();
        log.debug("Decrypted content: {}", decryptedContent);
        assertEquals(content, decryptedContent);
    }
    
    @Test
    public void testEncryptText2() throws Exception {
        PasswordProtection protection = PasswordProtectionBuilder.factory().aes(256).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
        String content = "hello world";
        String password = "password";
        ByteArrayResource resource = new ByteArrayResource();
        PasswordEncryptedFile enc = new PasswordEncryptedFile(resource, password, protection);
        enc.saveString(content);
        
        String encryptedContentEnvelope = new String(resource.toByteArray());
        log.debug("Input content: {}", content);
        log.debug("Encrypted file: {}", encryptedContentEnvelope);
        
        String decryptedContent = enc.loadString();
        log.debug("Decrypted content: {}", decryptedContent);
        assertEquals(content, decryptedContent);
        
    }
    
    @Test
    public void testDecryptText() throws Exception {
        String ciphertext = "-----BEGIN ENCRYPTED DATA-----\n" +
"Content-Encoding: base64\n" +
"Encryption-Algorithm: AES/CBC/PKCS5Padding\n" +
"Encryption-Key-Id: mhl78/TP4g4=:4bzsOCFP1BeY6unmsBDT0B0VxmW9fQO0bQh/6yZqgLY=\n" +
"Integrity-Algorithm: SHA256\n" +
"Key-Algorithm: PBKDF2WithHmacSHA1; iterations=1; key-length=128; salt-bytes=16\n" +
"\n" +
"tYk3OHzKZ3hyG73Mm/nvuts0bsa+x+59G8VuDGCEED8cyjegedwzB1pnL/bjkfUWoSlU71DuU43J\n" +
"Lu3ibINeRux4Nql10rXNW6LpJpg3Kdo=\n" +
"-----END ENCRYPTED DATA-----";
        String password = "password";
        ByteArrayResource resource = new ByteArrayResource(ciphertext.getBytes());
        PasswordEncryptedFile enc = new PasswordEncryptedFile(resource, password);
        String content = enc.loadString();
        log.debug("decrypted: {}", content);
    }
    
    @Test
    public void testFormatKeyAlgorithmHeader() {
        PasswordEncryptedFile.KeyAlgorithm info = new PasswordEncryptedFile.KeyAlgorithm();
        info.keyAlgorithm = "PBKDF2WithHmacSHA1";
        assertEquals("PBKDF2WithHmacSHA1", info.formatKeyAlgorithm());
        info.iterations = 1000;
        info.saltBytes = 8;
        assertEquals("PBKDF2WithHmacSHA1; iterations=1000; salt-bytes=8", info.formatKeyAlgorithm());
    }
    
    @Test
    public void testParseKeyAlgorithmHeader() {
        PasswordEncryptedFile.KeyAlgorithm a1 = new PasswordEncryptedFile.KeyAlgorithm();
        a1.parseKeyAlgorithm("PBKDF2WithHmacSHA1");
        assertEquals("PBKDF2WithHmacSHA1", a1.keyAlgorithm);
        
        PasswordEncryptedFile.KeyAlgorithm a2 = new PasswordEncryptedFile.KeyAlgorithm();
        a2.parseKeyAlgorithm("PBKDF2WithHmacSHA1; iterations=1000; salt-bytes=8");
        assertEquals("PBKDF2WithHmacSHA1", a2.keyAlgorithm);
        assertEquals(1000, a2.iterations);
        assertEquals(8, a2.saltBytes);
    }
}
