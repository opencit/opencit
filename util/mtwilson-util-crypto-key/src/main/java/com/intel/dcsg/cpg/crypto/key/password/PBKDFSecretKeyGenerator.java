/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key.password;

import com.intel.dcsg.cpg.crypto.key.Protection;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author jbuhacoff
 */
public class PBKDFSecretKeyGenerator implements SecretKeyGenerator {

    @Override
    public SecretKey generateSecretKey(String password, byte[] salt, PasswordProtection protection) {
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(protection.getKeyAlgorithm()); // keyAlgorithm == "PBKDF2WithHmacSHA1"
            SecretKey keyBytes = secretKeyFactory.generateSecret(new PBEKeySpec(password.toCharArray(), salt, protection.getIterations(), protection.getKeyLengthBits()));
            SecretKey key = new SecretKeySpec(keyBytes.getEncoded(), protection.getAlgorithm());  // algorithm == "AES"
            return key;
        } catch (Exception e) { // java7: (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
}
