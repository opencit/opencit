/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

//import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.key.EncryptionKeySource;
import com.intel.dcsg.cpg.crypto.key.Protection;
import com.intel.dcsg.cpg.crypto.key.ProtectionBuilder;
import com.intel.dcsg.cpg.crypto.key.RandomSource;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Compares time to re-use existing cipher instance vs using getInstance every time
 * @author jbuhacoff
 */
public class CipherPerformanceTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CipherPerformanceTest.class);
    private final int max = 1000;
    private static SecretKey key;
    private static RandomSource random;
    private static IvParameterSpec iv;
    private static byte[] plaintext;
    
    @BeforeClass
    public static void createKey() throws Exception {
        Protection protection = ProtectionBuilder.factory().aes(128).stream().sha256().build();
        EncryptionKeySource keySource = new EncryptionKeySource();
        key = keySource.getEncryptionKey(protection).getSecretKey();
        random = new RandomSource();
        iv = new IvParameterSpec(random.nextBytes(16));// block size for aes-128 hard-coded above
        plaintext = random.nextBytes(256); // arbitrary
    }
    
    /**
     * at 1,000 iterations,  we get testCipherNew 189 ms
     * at 10,000 iterations we get testCipherNew 1081 ms
     * at 100,000 iterations we got testCipherNew 19244 ms
     * at 100,000 iterations (same) testCipherNew 5292 ms
     * @throws Exception 
     */
    @Test
    public void testCipherNew() throws Exception {
        long start = System.currentTimeMillis();
        for(int i=0; i<max; i++) {
            Cipher cipher = Cipher.getInstance("AES/OFB8/NoPadding"); // corresponds to the protection initialized above
            cipher.init(Cipher.ENCRYPT_MODE, key, iv); 
            byte[] ciphertext = cipher.doFinal(plaintext);
        }
        long stop = System.currentTimeMillis();
        log.debug("testCipherNew {} ms", stop-start);
    }
    
    /***
     * at 1,000 iterations we get testCipherCache 161 ms     15% faster
     * at 10,000 iterations we get testCipherCache 487 ms    55% faster
     * at 100,000 iterations we got testCipherCache 13999 ms  17% faster  but repeated tests of this show that there's a pause occassionally and it can throw off the results by as much as 5 secnods on either method
     * at 100,000 iterations (same)  testCipherCache 3975 ms   25% faster
     * 
     * @throws Exception 
     */
    @Test
    public void testCipherCache() throws Exception {
        Cipher cipher = Cipher.getInstance("AES/OFB8/NoPadding"); // corresponds to the protection initialized above
        long start = System.currentTimeMillis();
        for(int i=0; i<max; i++) {
            cipher.init(Cipher.ENCRYPT_MODE, key, iv); 
            byte[] ciphertext = cipher.doFinal(plaintext);
        }
        long stop = System.currentTimeMillis();
        log.debug("testCipherCache {} ms", stop-start);
    }
    
}
