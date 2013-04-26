/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class for using password-based encryption 
 * @author jbuhacoff
 */
public class PasswordCipher {
    private final Logger log = LoggerFactory.getLogger(getClass());
//    private static final double DEFAULT_CIPHER_DELAY_TARGET_MILLIS = 100.0; 
    private transient String password;
    private String algorithm;
    private transient String cipherName;
    private transient String cipherMode;
    private transient String paddingMode;
    private CipherInfo cipherInfo;
    
    public PasswordCipher(String password) {
        setPassword(password);
//        setAlgorithm("PBEWithMD5AndDES/CBC/PKCS5Padding"); // kind of a weak default but my system doesn't have the newer  PBKDF2... // PBEWithSHA1AndDESede
        setAlgorithm("PBEWithSHA1AndDESede/CBC/PKCS5Padding"); // kind of a weak default but my system doesn't have the newer  PBKDF2... // PBEWithSHA1AndDESede
    }
    
    /**
     * You only need to call this if you want to change the password after you've created the PasswordCipher.
     * The new password will be used for subsequent encrypt/decrypt operations
     * @param password the user password
     */
    public final void setPassword(String password) { this.password = password; }
    
    /**
     * 
     * @param algorithm for example "PBEWithMD5AndDES/CBC/PKCS5Padding"
     */
    public final void setAlgorithm(String algorithm) { 
        this.algorithm = algorithm; 
        String part[] = algorithm.split("/"); // split "PBEWithMD5AndDES/CBC/PKCS5Padding" into PBEWithMD5AndDES, CBC, and PKCS5Padding
        if( part.length > 0 ) {
            cipherName = part[0];  // PBEWithMD5AndDES, PBEWithHmacSHA1AndDESede, PBKDF2WithHmacSHA1, ...
            cipherInfo = getCipherInfo(); // relies on cipherName being set
        }
        if( part.length > 1 ) {
            cipherMode = part[1];  // CBC, OFB8, ...
        }
        if( part.length > 2 ) {
            paddingMode = part[2]; // PKCS5Padding, NoPadding, ...
        }        
    }
    
    public final String getCipherName() { return cipherName; }
    public final String getCipherMode() { return cipherMode; }
    public final String getPaddingMode() { return paddingMode; }
    
    
    // XXX TODO setters for keybits, saltbytes... 
    
    private SecretKey createSecretKey(byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(cipherName); // throws NoSuchAlgorithmException
        SecretKey key = secretKeyFactory.generateSecret(new PBEKeySpec(password.toCharArray(), salt, cipherInfo.getIterations(), cipherInfo.getKeyBits())); // throws InvalidKeySpecException XXX is the 56-bit DES key length defined by a constant somewhere? use that instead, for clarity        
        return key;
    }
    
        
    /**
     * Always prepends the salt bytes to the data
     * @param data
     * @return 
     */
    public byte[] encrypt(byte[] input) throws CryptographyException {
        try {
            // first create a new salt and secret key
            byte[] salt = new byte[cipherInfo.getSaltBytes()];
            SecureRandom rnd = new SecureRandom();
            rnd.nextBytes(salt);
            SecretKey dek = createSecretKey(salt); // throws InvalidKeySpecException, NoSuchAlgorithmException
            // and use the key to encrypt the message
            AlgorithmParameterSpec dekParams = new PBEParameterSpec(salt, cipherInfo.getIterations()); // need to define the algorithm parameter specs because the cipher receives the Key interface which is generic... so it doesn't know about the parameters that are embedded in it
            Cipher cipher = Cipher.getInstance(algorithm); // throws NoSuchAlgorithmException, NoSuchPaddingException ; envelopeAlgorithm like "PBEWithHmacSHA1AndDESede/CBC/PKCS5Padding" 
            cipher.init(Cipher.ENCRYPT_MODE, dek, dekParams); // throws InvalidKeyException, InvalidAlgorithmParameterException        
            // encrypt and return the result
            byte[] ciphertext = cipher.doFinal(input); // throws IllegalBlockSizeException, BadPaddingException, we're assuming the signature value is base64-encoded
            return concat(salt, ciphertext);   // e.header contains the salt
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }
    
    /**
     * Always reads the salt bytes first
     * @param data
     * @return 
     */
    public byte[] decrypt(byte[] input) throws CryptographyException {
        try {
            // first get the salt bytes and create the secret key
            byte[] salt = new byte[cipherInfo.getSaltBytes()];
            System.arraycopy(input, 0, salt, 0, cipherInfo.getSaltBytes());
            SecretKey dek = createSecretKey(salt); // throws InvalidKeySpecException, NoSuchAlgorithmException
            // and use the key to decrypt the message
            AlgorithmParameterSpec dekParams = new PBEParameterSpec(salt, cipherInfo.getIterations()); // need to define the algorithm parameter specs because the cipher receives the Key interface which is generic... so it doesn't know about the parameters that are embedded in it
            Cipher cipher = Cipher.getInstance(algorithm); // throws NoSuchAlgorithmException, NoSuchPaddingException ; envelopeAlgorithm like "PBEWithHmacSHA1AndDESede/CBC/PKCS5Padding" 
            cipher.init(Cipher.DECRYPT_MODE, dek, dekParams); // throws InvalidKeyException, InvalidAlgorithmParameterException
            byte[] plaintext = cipher.doFinal(input, cipherInfo.getSaltBytes(), input.length-cipherInfo.getSaltBytes()); // throws IllegalBlockSizeException, BadPaddingException, we're assuming the signature value is base64-encoded
            return plaintext;
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }
    
    // XXX duplicated in Aes128
    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    
    /**
     * Precondition:  the cipherName variable is set to the algorithm name such as "PBEWithMD5AndDES"
     * Looping through the known enum values instead of using valueOf so that we can
     * return null instead of throwing an exception when it is not found.
     * @return a CipherInfo value or null if the current cipherName was not found in the list
     */
    private CipherInfo getCipherInfo() {
        for(CipherInfo cipher : CipherInfo.values()) {
            if( cipher.name().equalsIgnoreCase(getCipherName()) ) {
                return cipher;
            }
        }
        return null;
    }

    /**
     * XXX TODO  PasswordCipher needs a major refactor to support any other algorithm... this method just wont' work
     * because it's not just the key size and salt size that are different, but in the cipher code we need to instantiate
     * different parameter types for each one, and then each one needs to be serialized differently to capture those
     * parameters when we encrypt... THERE SHOULD be a way using the Java API to do this transparently using the getEncoded()
     * and then just annotte probably how many bytes that is first, then deserialize it, then get the rest of the data...
     * but even to set up the key for encryption we need different code for each algorithm.  
     * maybe using bouncy castle will help?  not sure if they have cleaned this up.
     */
    public static enum CipherInfo {
        PBEWithMD5AndDES(56,8,524288),
        PBEWithMD5AndTripleDES(168,8,2097152),
        PBEWithSHA1AndDESede(168,8,1048576);
//        PBKDF2WithHMACSHA1(256,16,1);   //  currently not supported because in the cipher code, when we create the cipher we then need to request an "AES" cipher, which is a different name, and has different initializatino code, and an IV that needs to be stored along with the salt...
        
        private int keybits;
        private int saltbytes;
        private int iterations;
        CipherInfo(int keybits, int saltbytes, int iterations) {
            this.keybits = keybits;
            this.saltbytes = saltbytes;
            this.iterations = iterations;
        }
        public int getKeyBits() { return keybits; }
        public int getSaltBytes() { return saltbytes; }
        public int getIterations() { return iterations; }
    }
    
    /**
     * 
     * @param cipherInfo the password-based cipher to test (algorithm name, salt size)
     * @param elapsedTimeTarget the minimum average delay;  the function will try increasingly larger iteration counts until the encryption operations takes MORE THAN this amount of time on average (5 data points in the average)
     * @return
     * @throws CryptographyException 
     */
    public int benchmarkIterationCount(CipherInfo cipherInfo, double elapsedTimeTarget) throws CryptographyException {
        try {
            Random rnd = new Random(); // don't need a secure random since we are not generating keys for production use here -- only to test encryption speed
            // generate random input for the trial
            byte[] plaintextInput = new byte[1024];
            rnd.nextBytes(plaintextInput);
            byte[] salt = new byte[cipherInfo.getSaltBytes()];
            rnd.nextBytes(salt);
            // prepare a key for the trial
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt, 1, cipherInfo.keybits); 
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(cipherInfo.name()); // throws NoSuchAlgorithmException
            SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);             // throws InvalidKeySpecException
            // now find out how many iterations it takes in order to "spin" for about 100ms 
            int iterationCount = 1;
            double avgElapsedTime = 0.0;
            while(iterationCount < Integer.MAX_VALUE && avgElapsedTime < elapsedTimeTarget) {
                iterationCount *= 2;
                avgElapsedTime = 0.0;
                for(int i=0; i<5; i++) { // 5 trials for each value of iteration count, in order to smooth out outliers due to transient spikes in system load 
                    long startTime = System.currentTimeMillis();
                    AlgorithmParameterSpec params = new PBEParameterSpec(salt, iterationCount); // need to define the algorithm parameter specs because the cipher receives the Key interface which is generic... so it doesn't know about the parameters that are embedded in it
                    Cipher cipher = Cipher.getInstance(cipherInfo.name()); // throws NoSuchAlgorithmException, NoSuchPaddingException ; envelopeAlgorithm like "PBEWithHmacSHA1AndDESede/CBC/PKCS5Padding" 
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
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }
    
}
