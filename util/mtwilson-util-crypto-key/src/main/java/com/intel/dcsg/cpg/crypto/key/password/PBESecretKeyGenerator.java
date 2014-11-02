/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key.password;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 *
 * @author jbuhacoff
 */
public class PBESecretKeyGenerator implements SecretKeyGenerator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PBESecretKeyGenerator.class);
    
    @Override
    public SecretKey generateSecretKey(String password, byte[] salt, PasswordProtection protection) {
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(protection.getKeyAlgorithm()); // in this case it would be PBEWithSHA1AndDESede; throws NoSuchAlgorithmException
            SecretKey key = secretKeyFactory.generateSecret(new PBEKeySpec(password.toCharArray(), salt, protection.getIterations(), protection.getKeyLengthBits())); // throws InvalidKeySpecException XXX is the 56-bit DES key length defined by a constant somewhere? use that instead, for clarity        
            log.debug("generated key length {}", key.getEncoded().length);
            return key;
        } catch (Exception e) { // java7: (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 
     * @param cipherInfo the password-based cipher to test (algorithm name, salt size)
     * @param elapsedTimeTarget the minimum average delay, in milliseconds;  the function will try increasingly larger iteration counts until the encryption operations takes MORE THAN this amount of milliseconds on average (5 data points in the average)
     * @return
     * @throws CryptographyException 
     */
    public int benchmarkIterationCount(String password, PasswordProtection protection, double elapsedTimeTarget) {
        try {
            SecureRandom rnd = new SecureRandom(); // don't really need a secure random since we are not generating keys for production use here -- only to test encryption speed
            // generate random input for the trial
            byte[] plaintextInput = new byte[1024];
            rnd.nextBytes(plaintextInput);
            byte[] salt = new byte[protection.getSaltBytes()];
            rnd.nextBytes(salt);
            // prepare a key for the trial
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt, 1, protection.getKeyLengthBits()); 
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(protection.getKeyAlgorithm()); // throws NoSuchAlgorithmException
            SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);             // throws InvalidKeySpecException
            // now find out how many iterations it takes in order to "spin" for about 100ms 
            int iterationCount = 1;
            double avgElapsedTime = 0.0;
            while(iterationCount < Integer.MAX_VALUE/2 && avgElapsedTime < elapsedTimeTarget) {
                iterationCount *= 2;
                avgElapsedTime = 0.0;
                for(int i=0; i<5; i++) { // 5 trials for each value of iteration count, in order to smooth out outliers due to transient spikes in system load 
                    long startTime = System.currentTimeMillis();
                    AlgorithmParameterSpec params = new PBEParameterSpec(salt, iterationCount); // need to define the algorithm parameter specs because the cipher receives the Key interface which is generic... so it doesn't know about the parameters that are embedded in it
                    Cipher cipher = Cipher.getInstance(protection.getAlgorithm()); // throws NoSuchAlgorithmException, NoSuchPaddingException ; envelopeAlgorithm like "PBEWithHmacSHA1AndDESede/CBC/PKCS5Padding" 
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, params); // throws InvalidKeyException, InvalidAlgorithmParameterException
                    /* byte[] ciphertext = */ cipher.doFinal(plaintextInput); // throws IllegalBlockSizeException, BadPaddingException;  ignore the returned cipher text, we are only interested in the performance here 
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - startTime;
                    avgElapsedTime = 1.0*(avgElapsedTime*i + elapsedTime) / (i+1);
                }
                log.debug("iterations: {}   avg elapsed time: {}", new Object[] { iterationCount, avgElapsedTime });
            }
            return iterationCount;
        }
        catch(NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }
    
}
