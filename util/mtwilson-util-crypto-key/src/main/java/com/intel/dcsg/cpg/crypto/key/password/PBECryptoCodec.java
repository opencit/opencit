/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key.password;

import com.intel.dcsg.cpg.crypto.key.RandomSource;
import com.intel.dcsg.cpg.io.ByteArray;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEParameterSpec;

/**
 *
 * @author jbuhacoff
 */
public class PBECryptoCodec implements CryptoCodec {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PBECryptoCodec.class);

    private String password;
    private PasswordProtection protection;
    private PBESecretKeyGenerator secretKeyGenerator = new PBESecretKeyGenerator();
    private RandomSource random = new RandomSource();

    public PBECryptoCodec(String password) {
        this.password = password;
    }
    public PBECryptoCodec(String password, PasswordProtection protection) {
        this.password = password;
        this.protection = protection;
    }
    
    public void setProtection(PasswordProtection protection) {
        this.protection = protection;
    }

    public PasswordProtection getProtection() {
        return protection;
    }
    
    
    @Override
    public byte[] encrypt(byte[] plaintext) {
        try {
            int saltN = protection.getSaltBytes(); // number of bytes to use for the salt
            log.debug("using {} salt bytes", saltN);
            // first create a new salt and secret key
            byte[] salt = random.nextBytes(saltN);
            log.debug("got {} salt bytes: {}", salt.length, salt);
            log.debug("key length {}", protection.getKeyLengthBits());
//            SecretKey dek = createSecretKey(salt); // throws InvalidKeySpecException, NoSuchAlgorithmException
            SecretKey dek = secretKeyGenerator.generateSecretKey(password, salt, protection);
            // and use the key to encrypt the message
            AlgorithmParameterSpec dekParams = new PBEParameterSpec(salt, protection.getIterations()); // need to define the algorithm parameter specs because the cipher receives the Key interface which is generic... so it doesn't know about the parameters that are embedded in it
//            Cipher cipher = Cipher.getInstance(algorithm); // throws NoSuchAlgorithmException, NoSuchPaddingException ; envelopeAlgorithm like "PBEWithHmacSHA1AndDESede/CBC/PKCS5Padding" 
            log.debug("algorithm {} cipher {}", protection.getAlgorithm(), protection.getCipher());
            Cipher cipher = Cipher.getInstance(protection.getCipher());  // cipher like AES/OFB8/NoPadding
            cipher.init(Cipher.ENCRYPT_MODE, dek, dekParams); // throws InvalidKeyException, InvalidAlgorithmParameterException        
            // encrypt and return the result
            byte[] ciphertext = cipher.doFinal(plaintext); // throws IllegalBlockSizeException, BadPaddingException, we're assuming the signature value is base64-encoded
            return ByteArray.concat(salt, ciphertext);   // e.header contains the salt
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
         try {
            int saltN = protection.getSaltBytes(); // number of bytes to use for the salt
            // first get the salt bytes and create the secret key
            byte[] salt = new byte[saltN];
            System.arraycopy(ciphertext, 0, salt, 0, saltN);
//            SecretKey dek = createSecretKey(salt); // throws InvalidKeySpecException, NoSuchAlgorithmException
            SecretKey dek = secretKeyGenerator.generateSecretKey(password, salt, protection);
            // and use the key to decrypt the message
            AlgorithmParameterSpec dekParams = new PBEParameterSpec(salt, protection.getIterations()); // need to define the algorithm parameter specs because the cipher receives the Key interface which is generic... so it doesn't know about the parameters that are embedded in it
//            Cipher cipher = Cipher.getInstance(algorithm); // throws NoSuchAlgorithmException, NoSuchPaddingException ; envelopeAlgorithm like "PBEWithHmacSHA1AndDESede/CBC/PKCS5Padding" 
            Cipher cipher = Cipher.getInstance(protection.getCipher()); // for example AES/OFB8/NoPadding
            cipher.init(Cipher.DECRYPT_MODE, dek, dekParams); // throws InvalidKeyException, InvalidAlgorithmParameterException
            byte[] plaintext = cipher.doFinal(ciphertext, saltN, ciphertext.length-saltN); // throws IllegalBlockSizeException, BadPaddingException, we're assuming the signature value is base64-encoded
            return plaintext;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
