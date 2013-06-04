/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.io.ByteArray;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class OpensslEncTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * for the md5 file (this is output from openssl -p option when the file was encrypted):
salt=3669C1FD6AB8B5C0
key=D2AEDB9B2FDEE8EB6BBD9BBA27493B06
iv =D56B1D9C9F3DEFBAE5D3314C4C03208C
     * 
     * in this test method we are getting the right salt, key, and iv from the file -- but then the
     * decryption results in garbage... and this is evident because trying to re-encrypt the decrypted
     * garbage results in a file completely different than the original encrypted file. 
     * 
     * password is confirmed to be correct because if you change it then you can't derive the correct
     * key and iv.
     * 
     * @throws Exception 
     */
    @Test
    public void testDecryptAes128OfbSha1() throws Exception {
        String password = "password";
        InputStream in = getClass().getResourceAsStream("/openssl-aes-128-ofb-md5.txt");
        String ciphertextBase64 = IOUtils.toString(in);
        IOUtils.closeQuietly(in);
        byte[] ciphertext = Base64.decodeBase64(ciphertextBase64);
        log.debug("Ciphertext length: {}", ciphertext.length);
            byte[] salt = new byte[8];
            System.arraycopy(ciphertext, 8, salt, 0, 8);
        log.debug("Salt: {}", Hex.encodeHexString(salt));
            
        byte[] passwordKey = Md5Digest.digestOf(ByteArray.concat(password.getBytes(), salt)).toByteArray();
//        byte[] passwordKeySha1 = Sha1Digest.digestOf(ByteArray.concat(password.getBytes(), salt)).toByteArray();
//        byte[] passwordKey = new byte[16]; // BLOCK_SIZE = 128 BITS / 8
//        System.arraycopy(passwordKeySha1, 0, passwordKey, 0, 16);  // if you don't truncate the key you will get InvalidKeyException: Invalid AES key length: 20 bytes
        log.debug("Key: {}", Hex.encodeHexString(passwordKey));
        byte[] iv = Md5Digest.digestOf(ByteArray.concat(passwordKey, password.getBytes(), salt)).toByteArray();
//        byte[] ivSha1 = Sha1Digest.digestOf(ByteArray.concat(passwordKey, password.getBytes(), salt)).toByteArray();
//        byte[] iv = new byte[16];
//        System.arraycopy(ivSha1, 0, iv, 0, 16);
        log.debug("IV: {}", Hex.encodeHexString(iv));
        
     String ALGORITHM = "AES/OFB8/NoPadding";
     int KEY_LENGTH = 128;
     int BLOCK_SIZE = 16; // KEY_LENGTH / 8
     SecretKey secretKey;
     Cipher cipher;
            cipher = Cipher.getInstance(ALGORITHM);
            secretKey = new SecretKeySpec(passwordKey, "AES"); // aes-128 implied by key length of 16 bytes = 128 bits
//            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ciphertext, 0, BLOCK_SIZE)); // this is what we have in Aes128
            
            
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] plaintext = cipher.doFinal(ciphertext, BLOCK_SIZE, ciphertext.length - BLOCK_SIZE); // skip the first 16 bytes (Salted__ + 8byte salt)
        log.debug("Plaintext length: {}", plaintext.length);
            System.out.println(new String(plaintext)); // uses ISO-8859-1,  but it's garbage
/*
            String correct = null;
            Map<String,Charset> charsets = Charset.availableCharsets();
            for(String charsetName : charsets.keySet()) {
                String plaintextStr = IOUtils.toString(plaintext, charsetName); //  "UTF-16"); // "ISO-8859-1"); // "UTF-8"); // when set to ISO-8859-1 looks same as above, when using UTF-8 it's still garbage but different
//                System.out.println(plaintextStr);
                if( plaintextStr.contains("MTWILSON")) { correct = charsetName; System.out.println(plaintextStr); break; }
            }
            if( correct != null ) { System.out.println("***** "+correct+" *******"); }
            */
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] ciphertext2 = cipher.doFinal(plaintext); // skip the first 16 bytes (Salted__ + 8byte salt)
        log.debug("ciphertext2 length: {}", ciphertext2.length);
            System.out.println(new String(Base64.encodeBase64Chunked(ciphertext2))); // uses ISO-8859-1,  but it's garbage
            
    }
    
    private byte[] swap(byte[] in) {
        byte[] out = new byte[in.length];
        for(int i=0; i<in.length; i++) {
            out[in.length-i-1] = in[i];
        }
        return out;
    }

    private byte[] swap1(byte[] in) {
        byte[] out = new byte[in.length];
        for(int i=0; i<in.length; i++) {
            out[in.length-i-1] = in[i];
        }
        return out;
    }
    
}
