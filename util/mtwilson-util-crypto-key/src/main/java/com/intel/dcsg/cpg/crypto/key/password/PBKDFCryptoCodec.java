/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key.password;

import com.intel.dcsg.cpg.crypto.key.Protection;
import com.intel.dcsg.cpg.crypto.key.RandomSource;
import com.intel.dcsg.cpg.io.ByteArray;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 *
 * @author jbuhacoff
 */
public class PBKDFCryptoCodec implements CryptoCodec {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PBKDFCryptoCodec.class);
    private String password;
    private PasswordProtection protection;
    private PBKDFSecretKeyGenerator secretKeyGenerator = new PBKDFSecretKeyGenerator();
    private RandomSource random = new RandomSource();

    public PBKDFCryptoCodec(String password) {
        this.password = password;
    }

    public PBKDFCryptoCodec(String password, PasswordProtection protection) {
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
            int saltN = protection.getSaltBytes();
            // first create a new salt and secret key
            byte[] salt = random.nextBytes(saltN);
//            SecretKey dek = createSecretKey(salt); // throws InvalidKeySpecException, NoSuchAlgorithmException
            SecretKey dek = secretKeyGenerator.generateSecretKey(password, salt, protection);
            log.debug("cipher {}", protection.getCipher());
            // and use the key to encrypt the message
            byte[] iv = generateIV(); //cipher.getIV();

            Cipher cipher = Cipher.getInstance(protection.getCipher()); // like AES/OFB8/NoPadding
            cipher.init(Cipher.ENCRYPT_MODE, dek, new IvParameterSpec(iv));
            byte[] ciphertext = cipher.doFinal(plaintext);
            return ByteArray.concat(salt, iv, ciphertext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
        try {
            int salt0 = 0;
            int saltN = protection.getSaltBytes();   // salt offset and size
            int iv0 = saltN;
            int ivN = protection.getBlockSizeBytes();   // iv offset and size
            int content0 = saltN + ivN;
            int contentN = ciphertext.length - content0;

            // first get the salt bytes and create the secret key
            byte[] salt = new byte[saltN];
            System.arraycopy(ciphertext, salt0, salt, 0, saltN);
            byte[] iv = new byte[ivN];
            System.arraycopy(ciphertext, iv0, iv, 0, ivN);

//            SecretKey dek = createSecretKey(salt); // throws InvalidKeySpecException, NoSuchAlgorithmException
            SecretKey dek = secretKeyGenerator.generateSecretKey(password, salt, protection);

            Cipher cipher = Cipher.getInstance(protection.getCipher()); // for example AES/OFB8/NoPadding
            cipher.init(Cipher.DECRYPT_MODE, dek, new IvParameterSpec(iv));
            return cipher.doFinal(ciphertext, content0, contentN); // skip the first 16 bytes (IV)
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private byte[] generateIV() {
        assert protection.getBlockSizeBytes() > 0;
        byte[] iv = random.nextBytes(protection.getBlockSizeBytes());
        return iv;
    }
    
}
