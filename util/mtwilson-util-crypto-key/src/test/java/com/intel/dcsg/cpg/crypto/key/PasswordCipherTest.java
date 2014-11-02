/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

//import com.intel.dcsg.cpg.crypto.PasswordCipher;
import com.intel.dcsg.cpg.crypto.key.password.PBECryptoCodec;
import com.intel.dcsg.cpg.crypto.key.password.PBESecretKeyGenerator;
import com.intel.dcsg.cpg.crypto.key.password.PBKDFCryptoCodec;
import com.intel.dcsg.cpg.crypto.key.password.PasswordCryptoCodecFactory;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
//import com.intel.dcsg.cpg.crypto.PasswordCipher.CipherInfo;
import org.apache.commons.codec.binary.Base64;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class PasswordCipherTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
//        setAlgorithm("PBEWithMD5AndDES/CBC/PKCS5Padding"); // kind of a weak default but my system doesn't have the newer  PBKDF2... // PBEWithSHA1AndDESede
//        setAlgorithm("PBEWithSHA1AndDESede/CBC/PKCS5Padding"); // kind of a weak default but my system doesn't have the newer  PBKDF2... // PBEWithSHA1AndDESede
    
    /**
     * Example output:
2014-03-03 17:10:26,948 DEBUG [main] c.i.d.c.c.PasswordCipherTest [PasswordCipherTest.java:28] encrypted hello world to C4ET9dyzebEhLG850ehOnhzABA34sWqD
2014-03-03 17:10:31,269 DEBUG [main] c.i.d.c.c.PasswordCipherTest [PasswordCipherTest.java:30] encrypted hello world to DeE5dHXJ/MMWh9DutARtotBOrPCZb/n+
     * 
     * @throws Exception 
     */
    @Test
    public void testEncryptPlaintextDefault3DES() throws Exception {
        String input = "hello world";
        PasswordProtection protection = PasswordProtectionBuilder.factory().algorithm("PBEWithMD5AndDES").digestAlgorithm("SHA-256").keyAlgorithm("PBEWithMD5AndDES").mode("CBC").padding("PKCS5Padding").build();
        PBECryptoCodec cipher = new PBECryptoCodec("password", protection);
//        PasswordCipher cipher = new PasswordCipher("password");
        String output1 = Base64.encodeBase64String(cipher.encrypt(input.getBytes())); // for example: yP+0upNme0MZVdAgYAnst1PTDAG2PviC 
        log.debug("encrypted {} to {}", input, output1);
        String output2 = Base64.encodeBase64String(cipher.encrypt(input.getBytes())); // for example: /AhjOh17Hm9mr0dd0Bu8dUjKyJkyyBvO
        log.debug("encrypted {} to {}", input, output2);
        assertNotEquals(output1, output2); // they should NOT be the same, because a different salt should be automatically used each time
    }
    
    /**
     * Example output:
     * 
     * @throws Exception 
     */
    @Test
    public void testEncryptPlaintextAES() throws Exception {
        String input = "hello world";
        PasswordProtection protection = PasswordProtectionBuilder.factory().aes(128).digestAlgorithm("SHA-256").keyAlgorithm("PBKDF2WithHmacSHA1").mode("CBC").padding("PKCS5Padding").build();
        PBKDFCryptoCodec cipher = new PBKDFCryptoCodec("password", protection);
//        PasswordCipher cipher = new PasswordCipher("password");
//        cipher.setAlgorithm("PBKDF2WithHmacSHA1AndAES128/CBC/PKCS5Padding");
        String output1 = Base64.encodeBase64String(cipher.encrypt(input.getBytes())); // for example: MloHljWIRCZqsHgKaBMnRcW72+8hmoGbadLzPDFJhQBVHDRpiWJ8MrClP5OoJ6Qm 
        log.debug("encrypted {} to {}", input, output1);
        String output2 = Base64.encodeBase64String(cipher.encrypt(input.getBytes())); // for example: pOV4bDv/AuYpWoYpuNe2CPrbRyPS8DCF832y6Qqxrf6wf0/Rng4W+FI2BQ86scoe
        log.debug("encrypted {} to {}", input, output2);
        assertNotEquals(output1, output2); // they should NOT be the same, because a different salt should be automatically used each time
    }

    @Test
    public void testEncryptPlaintextAES256() throws Exception {
        String input = "hello world";
        PasswordProtection protection = PasswordProtectionBuilder.factory().aes(256).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
        PBKDFCryptoCodec cipher = new PBKDFCryptoCodec("password", protection);
//        PasswordCipher cipher = new PasswordCipher("password");
//        cipher.setAlgorithm("PBKDF2WithHmacSHA1AndAES128/CBC/PKCS5Padding");
        String output1 = Base64.encodeBase64String(cipher.encrypt(input.getBytes())); // for example: MloHljWIRCZqsHgKaBMnRcW72+8hmoGbadLzPDFJhQBVHDRpiWJ8MrClP5OoJ6Qm 
        log.debug("encrypted {} to {}", input, output1);
        String output2 = Base64.encodeBase64String(cipher.encrypt(input.getBytes())); // for example: pOV4bDv/AuYpWoYpuNe2CPrbRyPS8DCF832y6Qqxrf6wf0/Rng4W+FI2BQ86scoe
        log.debug("encrypted {} to {}", input, output2);
        assertNotEquals(output1, output2); // they should NOT be the same, because a different salt should be automatically used each time
    }
    
    @Test
    public void testDecryptCiphertextDES() throws Exception {
        String input = "hello world";
        PasswordProtection protection = PasswordProtectionBuilder.factory().digestAlgorithm("SHA-256").keyAlgorithm("PBEWithMD5AndDES").mode("CBC").padding("PKCS5Padding").build();
        PBECryptoCodec cipher = new PBECryptoCodec("password", protection);
//        PasswordCipher cipher = new PasswordCipher("password");
        String ciphertext1 = Base64.encodeBase64String(cipher.encrypt(input.getBytes())); // for example "yP+0upNme0MZVdAgYAnst1PTDAG2PviC";
        String ciphertext2 = Base64.encodeBase64String(cipher.encrypt(input.getBytes())); // for example "/AhjOh17Hm9mr0dd0Bu8dUjKyJkyyBvO";
        String output1 = new String(cipher.decrypt(Base64.decodeBase64(ciphertext1))); // should be same as input: "hello world"
        log.debug("decrypted {} to {}", ciphertext1, output1);
        String output2 = new String(cipher.decrypt(Base64.decodeBase64(ciphertext2))); // should be same as input: "hello world"
        log.debug("decrypted {} to {}", ciphertext2, output2);
        assertEquals(output1, output2); // they SHOULD be the same
    }

    @Test
    public void testDecryptCiphertextAES() throws Exception {
        String input = "hello world";
        PasswordProtection protection = PasswordProtectionBuilder.factory().aes(128).digestAlgorithm("SHA-256").keyAlgorithm("PBKDF2WithHmacSHA1").iterations(1000).saltBytes(16).mode("CBC").padding("PKCS5Padding").build();
        PBKDFCryptoCodec cipher = new PBKDFCryptoCodec("password", protection);
//        PasswordCipher cipher = new PasswordCipher("password");
        String ciphertext1 = Base64.encodeBase64String(cipher.encrypt(input.getBytes())); // for example "t7FFzkqCWmv4aN+8UTMHAbtLHWC3yIdEo4V0PMiXR3VOSidetQIGiPFHQIUZ7kWg";
        String ciphertext2 = Base64.encodeBase64String(cipher.encrypt(input.getBytes())); // for example "yUBIj7ZC5zq5WS8rBdOKO9ktRjMTeAaweA5kyPTFyoSZzatHMUwQB4T6Ayf7AA/n";
        String output1 = new String(cipher.decrypt(Base64.decodeBase64(ciphertext1))); // should be same as input: "hello world"
        log.debug("decrypted {} to {}", ciphertext1, output1);
        String output2 = new String(cipher.decrypt(Base64.decodeBase64(ciphertext2))); // should be same as input: "hello world"
        log.debug("decrypted {} to {}", ciphertext2, output2);
        assertEquals(output1, output2); // they SHOULD be the same
    }
    
    @Test
    public void testValueOfCipherInfoEnum() {
        PasswordProtection protection = PasswordProtectionBuilder.factory().aes(128).digestAlgorithm("SHA-256").keyAlgorithm("PBEWithMD5AndDES").mode("CBC").padding("PKCS5Padding").build();
//        PBECryptoCodec cipher = new PBECryptoCodec("password", protection);
//        PasswordCipher cipher = new PasswordCipher("password");
//        CipherInfo info = PasswordCipher.CipherInfo.valueOf("PBEWithMD5AndDES");
        log.debug("PBEWithMD5AndDES key bits: {}", protection.getKeyLengthBits());
        log.debug("PBEWithMD5AndDES salt bytes: {}", protection.getSaltBytes());
    }
    
    @Test
    public void testListKnownCipherInfoEnum() {
        PasswordCryptoCodecFactory.EncryptionAlgorithmInfo[] knownCiphers = PasswordCryptoCodecFactory.EncryptionAlgorithmInfo.values();
        for(PasswordCryptoCodecFactory.EncryptionAlgorithmInfo i : knownCiphers) {
            log.debug("Known encryption cipher: {}", i.name());
        }
        PasswordCryptoCodecFactory.KeyAlgorithmInfo[] knownKeyAlgorithms = PasswordCryptoCodecFactory.KeyAlgorithmInfo.values();
        for(PasswordCryptoCodecFactory.KeyAlgorithmInfo i : knownKeyAlgorithms) {
            log.debug("Known key algorithm: {}", i.name());
        }
    }
    
    
//    @Test(expected=IllegalArgumentException.class)
//    public void testUnknownCipherInfo() {        
//        CipherInfo nonexistent = PasswordCipher.CipherInfo.valueOf("UnknownCipher"); // throws IllegalArgumentException
//    }
    
    /**
     * Test of the benchmark feature in PasswordCipher to help pick a suitable
     * number of iterations based on the performance of the local machine.
     * Of course if an attacker copies the password-protected file somewhere else with
     * more computing power and tries to crack it there, he will have better chances.
     * 
     * Sample output for PBEWithMD5AndDES:
2013-04-03 12:36:32,873 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 2   avg elapsed time: 3.4
2013-04-03 12:36:32,884 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 4   avg elapsed time: 1.4
2013-04-03 12:36:32,892 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 8   avg elapsed time: 1.6
2013-04-03 12:36:32,896 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 16   avg elapsed time: 0.6
2013-04-03 12:36:32,900 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 32   avg elapsed time: 0.6
2013-04-03 12:36:32,904 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 64   avg elapsed time: 0.8
2013-04-03 12:36:32,910 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 128   avg elapsed time: 1.2
2013-04-03 12:36:32,921 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 256   avg elapsed time: 2.2
2013-04-03 12:36:32,940 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 512   avg elapsed time: 3.8
2013-04-03 12:36:32,977 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 1024   avg elapsed time: 7.4
2013-04-03 12:36:33,002 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 2048   avg elapsed time: 5.0
2013-04-03 12:36:33,011 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 4096   avg elapsed time: 1.8
2013-04-03 12:36:33,023 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 8192   avg elapsed time: 2.4
2013-04-03 12:36:33,047 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 16384   avg elapsed time: 4.6
2013-04-03 12:36:33,095 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 32768   avg elapsed time: 9.6
2013-04-03 12:36:33,188 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 65536   avg elapsed time: 18.6
2013-04-03 12:36:33,357 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 131072   avg elapsed time: 33.8
2013-04-03 12:36:33,700 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 262144   avg elapsed time: 68.6
2013-04-03 12:36:34,381 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 524288   avg elapsed time: 136.0
2013-04-03 12:36:35,739 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 1048576   avg elapsed time: 271.6
2013-04-03 12:36:38,429 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 2097152   avg elapsed time: 537.8
2013-04-03 12:36:43,800 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:211] iterations: 4194304   avg elapsed time: 1074.2
2013-04-03 12:36:43,801 DEBUG [main] c.i.d.c.PasswordCipherTest [PasswordCipherTest.java:80] suggested iterations: 4194304
     * 
     * Sample output for PBEWithMD5AndTripleDES:
2013-04-03 13:40:40,701 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 2   avg elapsed time: 6.0
2013-04-03 13:40:40,709 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 4   avg elapsed time: 0.6
2013-04-03 13:40:40,713 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 8   avg elapsed time: 0.8
2013-04-03 13:40:40,720 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 16   avg elapsed time: 1.2
2013-04-03 13:40:40,728 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 32   avg elapsed time: 1.6
2013-04-03 13:40:40,735 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 64   avg elapsed time: 1.4
2013-04-03 13:40:40,746 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 128   avg elapsed time: 2.0
2013-04-03 13:40:40,767 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 256   avg elapsed time: 4.0
2013-04-03 13:40:40,803 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 512   avg elapsed time: 7.2
2013-04-03 13:40:40,840 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 1024   avg elapsed time: 7.4
2013-04-03 13:40:40,852 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 2048   avg elapsed time: 2.2
2013-04-03 13:40:40,870 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 4096   avg elapsed time: 3.6
2013-04-03 13:40:40,898 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 8192   avg elapsed time: 5.6
2013-04-03 13:40:40,949 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 16384   avg elapsed time: 10.2
2013-04-03 13:40:41,048 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 32768   avg elapsed time: 19.6
2013-04-03 13:40:41,230 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 65536   avg elapsed time: 36.2
2013-04-03 13:40:41,604 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 131072   avg elapsed time: 74.6
2013-04-03 13:40:42,344 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 262144   avg elapsed time: 148.0
2013-04-03 13:40:43,847 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 524288   avg elapsed time: 300.4
2013-04-03 13:40:46,916 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 1048576   avg elapsed time: 613.8
2013-04-03 13:40:52,811 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:220] iterations: 2097152   avg elapsed time: 1179.0
2013-04-03 13:40:52,811 DEBUG [main] c.i.d.c.PasswordCipherTest [PasswordCipherTest.java:106] suggested iterations: 2097152
     * 
     * 
     * Sample output for PBEWithSHA1AndDESede:
2013-04-03 13:59:43,313 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 2   avg elapsed time: 6.0
2013-04-03 13:59:43,320 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 4   avg elapsed time: 0.6
2013-04-03 13:59:43,324 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 8   avg elapsed time: 0.6
2013-04-03 13:59:43,328 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 16   avg elapsed time: 0.8
2013-04-03 13:59:43,332 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 32   avg elapsed time: 0.8
2013-04-03 13:59:43,336 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 64   avg elapsed time: 0.8
2013-04-03 13:59:43,341 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 128   avg elapsed time: 0.8
2013-04-03 13:59:43,349 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 256   avg elapsed time: 1.6
2013-04-03 13:59:43,360 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 512   avg elapsed time: 2.0
2013-04-03 13:59:43,377 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 1024   avg elapsed time: 3.2
2013-04-03 13:59:43,395 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 2048   avg elapsed time: 3.2
2013-04-03 13:59:43,424 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 4096   avg elapsed time: 5.8
2013-04-03 13:59:43,477 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 8192   avg elapsed time: 10.6
2013-04-03 13:59:43,588 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 16384   avg elapsed time: 22.0
2013-04-03 13:59:43,790 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 32768   avg elapsed time: 40.2
2013-04-03 13:59:44,191 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 65536   avg elapsed time: 80.0
2013-04-03 13:59:45,009 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 131072   avg elapsed time: 163.4
2013-04-03 13:59:46,606 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 262144   avg elapsed time: 319.4
2013-04-03 13:59:49,818 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 524288   avg elapsed time: 642.2
2013-04-03 13:59:56,199 DEBUG [main] c.i.d.c.PasswordCipher [PasswordCipher.java:216] iterations: 1048576   avg elapsed time: 1276.0
2013-04-03 13:59:56,199 DEBUG [main] c.i.d.c.PasswordCipherTest [PasswordCipherTest.java:131] suggested iterations: 1048576     * 
     * @throws Exception 
     */
    @Test
    public void testGuessSuitableIterationCount() throws Exception { // not found: PBKDF2WithHMACSHA1
        PasswordProtection protection = PasswordProtectionBuilder.factory().aes(128).digestAlgorithm("SHA-256").keyAlgorithm("PBEWithMD5AndDES").mode("CBC").padding("PKCS5Padding").build();
//        PBECryptoCodec cipher = new PBECryptoCodec("password", protection);
        PBESecretKeyGenerator secretKeyGenerator = new PBESecretKeyGenerator();
//        PasswordCipher cipher = new PasswordCipher("password");
//       CipherInfo info = PasswordCipher.CipherInfo.valueOf("PBEWithMD5AndDES"); // works: PBEWithMD5AndDES, PBEWithMD5AndTripleDES , PBEWithSHA1AndDESede
//        PasswordCipher cipher = new PasswordCipher("password");
        int numIterations = secretKeyGenerator.benchmarkIterationCount("password", protection, 1000.0);  // find how many iterations are necessary in order to cause the encryption operation to take at least 1000ms
        log.debug("suggested iterations: {}", numIterations);
    }
}
