/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Set;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class JavaCipherTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * This test demonstrates that it is unsafe to use the default settings for PBEKeySpec  with PBEWithMD5AndDES:
     * 
     * Default salt is 8 bytes of zero
     * Default iteration count is 1
     * Therefore Same password always generates the same key...
     * For example, with default values the password "password" always generates the key "cGFzc3dvcmQ=" (base64 encoded) 
     * 
     * @throws CryptographyException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException 
     */
    @Test
    public void testDefaultPBEDESSettings() throws CryptographyException, NoSuchAlgorithmException, InvalidKeySpecException {
        // test default settings
        String password = "password";
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        log.debug("Password: {}", new String(pbeKeySpec.getPassword()));
//        log.debug("Salt length: {}", pbeKeySpec.getSalt().length); // throws NullPointerException because salt was not set !!  which also means we can't auto-detect required salt length by not providing one... we need to just know.
        log.debug("Salt base64: {}", Base64.encodeBase64String(pbeKeySpec.getSalt())); // null because salt was not set !!   
        log.debug("Iteration count:: {}", pbeKeySpec.getIterationCount()); // zero because it was not set !!
        log.debug("Key length: {}", pbeKeySpec.getKeyLength()); // zero because it was not set !!

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES"); // throws NoSuchAlgorithmException
        SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec); // throws InvalidKeySpecException
        log.debug("Secret key algorithm: {}", secretKey.getAlgorithm());
        log.debug("Secret key base64: {}", Base64.encodeBase64String(secretKey.getEncoded())); //   cGFzc3dvcmQ=    ... ALWAYS the same value because we did not set a salt, and it's using ZERO iterations probably
        
        /*
        SecretKey secretKey = secretKeyFactory.generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, keybits)); // throws InvalidKeySpecException XXX is the 56-bit DES key length defined by a constant somewhere? use that instead, for clarity        
*/
    }
    
    /**
     * Sad result: default number of iterations is ONE
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException 
     */
    @Test
    public void testDefaultNumberOfIterations() throws InvalidKeySpecException, NoSuchAlgorithmException {
        String password = "password";
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES"); // throws NoSuchAlgorithmException
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);             // throws InvalidKeySpecException
        // a little experiment to find out what is the default number of iterations used, if we don't specify it:
        for(int i=1; i<1024; i++) {  //  cannot start at zero, that's an invalid iteration count value
            PBEKeySpec pbeKeySpecX = new PBEKeySpec(password.toCharArray(), new byte[] {0, 0, 0, 0, 0, 0, 0, 0 /* 8 */, 0, 0, 0, 0, 0, 0, 0, 0 /*16*/, 0, 0, 0, 0, 0, 0, 0, 0 /*24*/}, i, 56); // a keyspec with no salt (as above) and variable iteration cont;  "no salt" means 8 bytes of zero, as it's not allowed to be empty or null (internally if you don't set it, then PBEKeySpec uses an array of zeros too)
            SecretKey secretKeyX = secretKeyFactory.generateSecret(pbeKeySpecX); 
            if( Arrays.equals(secretKey.getEncoded(), secretKeyX.getEncoded()) ) {
                log.debug("Default number of iterations: {}", i);  //   the output is ONE !!!   default number of iterations used is "1" if you don't specify it
                break;
            } 
        }        
    }

    /**
     * Ideally... the number of iterations chosen is sufficiently high to create a barrier to 
     * dictionary attacks by delaying the attacker's computations... some people recommend that
     * the password derivation should take 100ms ... 
     * These results will vary according to your computer hardware.
     * On an Intel Core i5-2540M CPU @ 2.60GHz  I got tired of waiting for the elapsed time to be even 1ms... and
     * the iteration count was 1,208,718 when I stopped the loop,  and skipped straight 
     * to Integer.MAX_VALUE:  2,147,483,647  ... and that took approx. 1ms.
     * And anyway after Integer.MAX_VALUE iterations, secretKey.getEncoded() is the same value as it was for 1 iteration... so it must not be using that iteration count when creating the key... dont' know why it's even an option... or maybe because THAT PARTICULAR ALGORITHM does not have variable iteration count but it happens that the java api allows us to uselessly specify one
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException 
     */
    @Test
    public void testNominalKeygenIterationCount() throws InvalidKeySpecException, NoSuchAlgorithmException {
        String password = "password";
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES"); // throws NoSuchAlgorithmException
        // now find out how many iterations it takes in order to "spin" for about 100ms 
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        int iterationCount = Integer.MAX_VALUE-1;
        // byte[] salt = new byte[] { 0,0,0,0, 0,0,0,0 }         
        byte[] salt = new byte[8];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(salt);
        while(iterationCount < Integer.MAX_VALUE) { //  do this to find out in one step what is the delay when you use Integer.MAX_VALUE iterations
//         while(currentTime < startTime+100) {    // do this to start at one iteration and work your way up... warning... it is futile on modern systems, since Integer.MAX_VALUE produced just 1ms delay
            startTime = System.currentTimeMillis();
            iterationCount++;
            PBEKeySpec pbeKeySpecX = new PBEKeySpec(password.toCharArray(), salt, iterationCount, 56); // a keyspec with no salt (as above) and variable iteration cont; 
            SecretKey secretKeyX = secretKeyFactory.generateSecret(pbeKeySpecX);             // throws InvalidKeySpecException
            currentTime = System.currentTimeMillis();
            log.debug("iterations: {}   elapsed time: {}     derived key: {}", new Object[] { iterationCount, currentTime-startTime, Base64.encodeBase64String(secretKeyX.getEncoded()) });
        }        
    }

    /**
     * Here is sample output running on an an Intel Core i5-2540M CPU @ 2.60GHz :
2013-04-03 11:52:14,629 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 1   avg elapsed time: 2.4
2013-04-03 11:52:14,633 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 2   avg elapsed time: 0.0
2013-04-03 11:52:14,635 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 4   avg elapsed time: 0.4
2013-04-03 11:52:14,636 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 8   avg elapsed time: 0.2
2013-04-03 11:52:14,638 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 16   avg elapsed time: 0.4
2013-04-03 11:52:14,640 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 32   avg elapsed time: 0.4
2013-04-03 11:52:14,643 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 64   avg elapsed time: 0.6
2013-04-03 11:52:14,649 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 128   avg elapsed time: 1.2
2013-04-03 11:52:14,659 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 256   avg elapsed time: 2.0
2013-04-03 11:52:14,678 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 512   avg elapsed time: 3.6
2013-04-03 11:52:14,713 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 1024   avg elapsed time: 7.0
2013-04-03 11:52:14,731 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 2048   avg elapsed time: 3.6
2013-04-03 11:52:14,738 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 4096   avg elapsed time: 1.4
2013-04-03 11:52:14,751 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 8192   avg elapsed time: 2.6
2013-04-03 11:52:14,776 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 16384   avg elapsed time: 4.8
2013-04-03 11:52:14,821 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 32768   avg elapsed time: 9.0
2013-04-03 11:52:14,917 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 65536   avg elapsed time: 19.2
2013-04-03 11:52:15,085 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 131072   avg elapsed time: 33.6
2013-04-03 11:52:15,427 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 262144   avg elapsed time: 68.4
2013-04-03 11:52:16,102 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 524288   avg elapsed time: 134.8       <--- first one >100ms at 525k iterations
2013-04-03 11:52:17,460 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 1048576   avg elapsed time: 271.4
2013-04-03 11:52:20,161 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 2097152   avg elapsed time: 540.2
2013-04-03 11:52:25,500 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 4194304   avg elapsed time: 1067.8      <--- first one >1s at 4,194k (4M) iterations
2013-04-03 11:52:36,218 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 8388608   avg elapsed time: 2143.6
2013-04-03 11:52:57,557 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 16777216   avg elapsed time: 4267.8
2013-04-03 11:53:40,389 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 33554432   avg elapsed time: 8566.4
2013-04-03 11:55:12,926 DEBUG [main] c.i.d.c.JavaCipherTest [JavaCipherTest.java:169] iterations: 67108864   avg elapsed time: 18507.4    <--- first one >10s at 67,108k (67M) iterations
     * 
     * Running the test again with an input that was 100x the size of the first trial, only increased the avg elapsed times by about 10%
     * 
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     */
    @Test
    public void testNominalCipherIterationCount() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String input = "hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|hello world|";
        log.debug("length of input: {}", input.length());
        String password = "password"; 
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES"); // throws NoSuchAlgorithmException
        // now find out how many iterations it takes in order to "spin" for about 100ms 
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        int iterationCount = 1;//Integer.MAX_VALUE-1;
//        HashMap<Integer,Double> perfdata = new HashMap<Integer,Double>();  //  iteration count -> avg cipher time
         byte[] salt = new byte[] { 0,0,0,0, 0,0,0,0 };
//        byte[] salt = new byte[8];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(salt);
        PBEKeySpec pbeKeySpecX = new PBEKeySpec(password.toCharArray(), salt, iterationCount, 56); // a keyspec with no salt (as above) and variable iteration cont; 
        SecretKey secretKeyX = secretKeyFactory.generateSecret(pbeKeySpecX);             // throws InvalidKeySpecException
//         while(currentTime < startTime+100) {    // do this to start at one iteration and work your way up... warning... it is futile on modern systems, since Integer.MAX_VALUE produced just 1ms delay
         while(currentTime < startTime+100 && iterationCount < Integer.MAX_VALUE) {    // do this to start at one iteration and work your way up... warning... it is futile on modern systems, since Integer.MAX_VALUE produced just 1ms delay
             // try 5 times with each iteration count setting to get an average value
             double avgElapsedTime = 0.0;
             for(int i=0; i<5; i++) {
                startTime = System.currentTimeMillis();
                AlgorithmParameterSpec params = new PBEParameterSpec(salt, iterationCount); // need to define the algorithm parameter specs because the cipher receives the Key interface which is generic... so it doesn't know about the parameters that are embedded in it
                Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES"); // throws NoSuchAlgorithmException, NoSuchPaddingException ; envelopeAlgorithm like "PBEWithHmacSHA1AndDESede/CBC/PKCS5Padding" 
                cipher.init(Cipher.ENCRYPT_MODE, secretKeyX, params); // throws InvalidKeyException, InvalidAlgorithmParameterException
                byte[] ciphertext = cipher.doFinal(input.getBytes()); // throws IllegalBlockSizeException, BadPaddingException, we're assuming the signature value is base64-encoded
                currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                avgElapsedTime = 1.0*(avgElapsedTime*i + elapsedTime) / (i+1);
             }
            log.debug("iterations: {}   avg elapsed time: {}", new Object[] { iterationCount, avgElapsedTime });
            iterationCount *= 2;
         }
    }
    
    
    @Test
    public void testDefaultPBETripleDESSettings() throws CryptographyException, NoSuchAlgorithmException, InvalidKeySpecException {
        // test default settings
        String password = "password";
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        log.debug("Password: {}", new String(pbeKeySpec.getPassword()));
//        log.debug("Salt length: {}", pbeKeySpec.getSalt().length); // throws NullPointerException because salt was not set !!  which also means we can't auto-detect required salt length by not providing one... we need to just know.
        log.debug("Salt base64: {}", Base64.encodeBase64String(pbeKeySpec.getSalt())); // null because salt was not set !!   
        log.debug("Iteration count:: {}", pbeKeySpec.getIterationCount()); // zero because it was not set !!
        log.debug("Key length: {}", pbeKeySpec.getKeyLength()); // zero because it was not set !!

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndTripleDES"); // throws NoSuchAlgorithmException
        SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec); // throws InvalidKeySpecException
        log.debug("Secret key algorithm: {}", secretKey.getAlgorithm());
        log.debug("Secret key base64: {}", Base64.encodeBase64String(secretKey.getEncoded())); //   cGFzc3dvcmQ=    ... ALWAYS the same value because we did not set a salt, and it's using ZERO iterations probably ... and key length is zero!!  so get same value with TripleDES as you do with DES
        
        // a little experiment to find out what is the default number of iterations used, if we don't specify it:
        for(int i=1; i<1024; i++) {  //  cannot start at zero, that's an invalid iteration count value
            PBEKeySpec pbeKeySpecX = new PBEKeySpec(password.toCharArray(), new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, i); // a keyspec with no salt (as above) and variable iteration cont;  ... damn it's not allowed to be empty or null!!            
            SecretKey secretKeyX = secretKeyFactory.generateSecret(pbeKeySpecX); 
            if( Arrays.equals(secretKey.getEncoded(), secretKeyX.getEncoded()) ) {
                log.debug("Default number of iterations: {}", i);  //   the output is ONE !!!   default number of iterations used is "1" if you don't specify it
                break;
            } 
        }
         
        /*
        SecretKey secretKey = secretKeyFactory.generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, keybits)); // throws InvalidKeySpecException XXX is the 56-bit DES key length defined by a constant somewhere? use that instead, for clarity        
*/
    }
    
 /**
  * 
  * PBEWithMD5AndDESede  ... key factory not available
  * PBEWithMD5AndTripleDES  ... key factory works but cipher always complains about illegal key size... tried everything between 1 and 256... and "TripleDES" is not even listed on the java cipher names page! 
  * 
  * TripleDES uses the PASSWORD as the key, and it must be ASCII
  * 
  * java.security.IllegalKeyParameterException: Illegal key size     <----------  "illegal" indicates you need to install the JCE Unlimited STrength policy files from Oracle into your JRE/lib/security folder...  "wrong" indicates you chose a value incompatible with the algorithm
  * java.security.InvalidAlgorithmParameterException: Salt must be 8 bytes long
  * 
  * @throws InvalidKeySpecException
  * @throws NoSuchAlgorithmException 
  */
    
    @Test
    public void testTripleDESKeySize() throws InvalidKeySpecException, NoSuchAlgorithmException {
        String plaintextInput = "hello world";
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndTripleDES"); // throws NoSuchAlgorithmException 
         byte[] salt = new byte[] { 1,2,3,4, 5,6,7,8 }         ;
         int keybits = 0;
        while(keybits < 256) { //  do this to find out in one step what is the delay when you use Integer.MAX_VALUE iterations
            keybits += 8;
            char[] password = new char[keybits/8];
            for(int i=0; i<password.length; i++) { password[i] = 'a'; }
//        String password = "passwordpasswordpassword";  // 24 bytes... used directly as the desede key ???
            try {
                PBEKeySpec pbeKeySpecX = new PBEKeySpec(password , salt, 1 , 192); // a keyspec with no salt (as above) and variable iteration cont; 
                SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpecX);             // throws InvalidKeySpecException
//                SecretKey secretKeyX = new SecretKeySpec(secretKey.getEncoded(), "PBEWithMD5AndDESede"); // hmmm... why dowe need to do this extra step??
                AlgorithmParameterSpec params = new PBEParameterSpec(salt, 1); // need to define the algorithm parameter specs because the cipher receives the Key interface which is generic... so it doesn't know about the parameters that are embedded in it
                Cipher cipher = Cipher.getInstance("PBEWithMD5AndTripleDES"); // throws NoSuchAlgorithmException, NoSuchPaddingException ; envelopeAlgorithm like "PBEWithHmacSHA1AndDESede/CBC/PKCS5Padding" 
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, params); // throws InvalidKeyException, InvalidAlgorithmParameterException
                /* byte[] ciphertext = */ cipher.doFinal(plaintextInput.getBytes()); // throws IllegalBlockSizeException, BadPaddingException;  ignore the returned cipher text, we are only interested in the performance here 
                log.debug("key length {} is valid!", keybits);
            }
            catch(Exception e) {
                log.debug("key length {} is not valid: {}", keybits, e.toString());
            }
        }        
        
    }

    
        /**
         * Sample output:

BLOWFISH
PBEWITHSHA1ANDDESEDE
AESWRAP
DESEDE
DES
AES
DESEDEWRAP
ARCFOUR
RSA/ECB/PKCS1PADDING
RC2
PBEWITHMD5ANDDES
PBEWITHSHA1ANDRC2_40
RSA
PBEWITHMD5ANDTRIPLEDES
* 
         */
    @Test
    public void testListCiphers() {
        System.out.println("\n\nAVAILABLE CIPHERS\n");
        Set ciphers = Security.getAlgorithms("Cipher");
        Object[] cipherNames = ciphers.toArray();			
        for (int i = 0; i < cipherNames.length; i++) {
            System.out.println(cipherNames[i]);
        }        
    }

    /**
sample output:
* 
PBKDF2WITHHMACSHA1
PBEWITHSHA1ANDDESEDE
DESEDE
DES
PBEWITHMD5ANDDES
PBEWITHSHA1ANDRC2_40
PBEWITHMD5ANDTRIPLEDES  
* 
     */
    @Test
    public void testListSecretKeyAlgorithms() {
        System.out.println("\n\nAVAILABLE SECRET KEY ALGORITHMS\n");
        Set ciphers = Security.getAlgorithms("SecretKeyFactory");
        Object[] cipherNames = ciphers.toArray();			
        for (int i = 0; i < cipherNames.length; i++) {
            System.out.println(cipherNames[i]);
        }        
    }
    
    /**
     * After the JCE unlimited strength policy files are installed, I get these results... which is worthless because some of the ciphers can't even handle key sizes that large... so this is just a policy reflection, not information from the engine.
         * Sample output with Security.getAlgorithms(Cipher):
BLOWFISH              : 2147483647bit
PBEWITHSHA1ANDDESEDE  : 2147483647bit
AESWRAP               : 2147483647bit
DESEDE                : 2147483647bit
DES                   : 2147483647bit
AES                   : 2147483647bit
DESEDEWRAP            : 2147483647bit
ARCFOUR               : 2147483647bit
RSA/ECB/PKCS1PADDING  : 2147483647bit
RC2                   : 2147483647bit
PBEWITHMD5ANDDES      : 2147483647bit
PBEWITHSHA1ANDRC2_40  : 2147483647bit
RSA                   : 2147483647bit
PBEWITHMD5ANDTRIPLEDES: 2147483647bit     * 
     * 
     * Sample output with Security.getAlgorithms(SecretKeyFactory)
PBKDF2WITHHMACSHA1    : 2147483647bit
PBEWITHSHA1ANDDESEDE  : 2147483647bit
DESEDE                : 2147483647bit
DES                   : 2147483647bit
PBEWITHMD5ANDDES      : 2147483647bit
PBEWITHSHA1ANDRC2_40  : 2147483647bit
PBEWITHMD5ANDTRIPLEDES: 2147483647bit     * 
     */
    @Test
    public void testMaxKeySize() {
        try {
            Set<String> algorithms = Security.getAlgorithms("Cipher");
            for(String algorithm: algorithms) {
                int max;
                max = Cipher.getMaxAllowedKeyLength(algorithm);
                System.out.printf("%-22s: %dbit%n", algorithm, max);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }        
    }
    
}
