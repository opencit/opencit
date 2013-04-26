/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.Aes128;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
public class Aes128Test {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testGenerateAesKey() throws CryptographyException {
        SecretKey key = Aes128.generateKey();
        System.out.println(Base64.encodeBase64String(key.getEncoded()));
    }
    
    /**
     * Example output:
Input: hello world (str length: 11)
Secret Key: 5v99AOdjFzG8GsA7qZsYRQ== (raw length: 16)
Plaintext: aGVsbG8gd29ybGQ= (raw length: 11)
Ciphertext: n5sXfRjdiwI1x2r1t5CLLdH3/vH2IEkxkCmd (raw length: 27)
Decrypted Plaintext: aGVsbG8gd29ybGQ= (raw length: 11)
Output: hello world (str length: 11)
     * 
     * @throws CryptographyException
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testEncryptDecrypt() throws CryptographyException, UnsupportedEncodingException {
        String input = "hello world";
        log.info("Input: {} (str length: {})", new String[] { input, String.valueOf(input.length()) });
        
        SecretKey secretKey = Aes128.generateKey(); // NoSuchAlgorithmException
        log.info("Secret Key: {} (raw length: {})", new String[] { Base64.encodeBase64String(secretKey.getEncoded()), String.valueOf(secretKey.getEncoded().length) });
        Aes128 aes = new Aes128(secretKey); // NoSuchPaddingException
        
        byte[] plaintext = input.getBytes("UTF-8"); //UnsupportedEncodingException
        byte[] ciphertext = aes.encrypt(plaintext); // InvalidKeyException, IllegalBlockSizeException, BadPaddingException
        
        log.info("Plaintext: {} (raw length: {})", new String[] { Base64.encodeBase64String(plaintext), String.valueOf(plaintext.length) });
        log.info("Ciphertext: {} (raw length: {})", new String[] { Base64.encodeBase64String(ciphertext), String.valueOf(ciphertext.length) });
        
        byte[] decrypted = aes.decrypt(ciphertext);
        log.info("Decrypted Plaintext: {} (raw length: {})", new String[] { Base64.encodeBase64String(decrypted), String.valueOf(decrypted.length) });
        String output = new String(decrypted, "UTF-8");
        log.info("Output: {} (str length: {})", new String[] { output, String.valueOf(output.length()) });
        
        assertEquals(input, output);
        
    }
    
    @Test
    public void testStreamEncryptDecrypt() throws CryptographyException, IOException {
        // create a random input stream
        Random random = new Random();
        byte[] plaintext = new byte[5963];   // purposefully an odd number and not a multiple of 2 so it definitely won't align with any blocks
        random.nextBytes(plaintext);
        log.info("Plaintext length {} content: {}", plaintext.length, Base64.encodeBase64String(plaintext));
        ByteArrayInputStream plaintextSourceIn = new ByteArrayInputStream(plaintext);
        // create a ciphertext storage
        ByteArrayOutputStream cipherOut = new ByteArrayOutputStream();
        // generate a new random key and encrypt the input stream
        SecretKey key = Aes128.generateKey();
        Aes128 aes = new Aes128(key);
        aes.encryptStream(plaintextSourceIn, cipherOut); // throws CryptographyException, IOException
        // check the output
        byte[] ciphertext = cipherOut.toByteArray();
        log.info("Ciphertext length {} content: {}", ciphertext.length, Base64.encodeBase64String(ciphertext));
        // decrypt it again
        ByteArrayInputStream cipherIn = new ByteArrayInputStream(ciphertext);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        aes.decryptStream(cipherIn, out);
        // check the output
        byte[] plaintext2 = out.toByteArray();
        log.info("Plaintext length {} content: {}", plaintext2.length, Base64.encodeBase64String(plaintext2));        
        assertTrue(Arrays.equals(plaintext, plaintext2));
    }
}
