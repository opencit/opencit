/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.authz.token;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.key.EncryptionKeySource;
import com.intel.dcsg.cpg.crypto.key.Protection;
import com.intel.dcsg.cpg.crypto.key.ProtectionBuilder;
import com.intel.dcsg.cpg.crypto.key.RandomSource;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Compares time to re-use existing cipher instance vs using getInstance every time
 * @author jbuhacoff
 */
public class DigestPerformanceTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigestPerformanceTest.class);
    private final int max = 1000000;
    private static RandomSource random;
    private static byte[] plaintext;
    
    @BeforeClass
    public static void createKey() throws CryptographyException {
        random = new RandomSource();
        plaintext = random.nextBytes(256); // arbitrary
    }
    
    /**
     * at 1,000 iterations,  we get testDigestNew 40 ms
     * at 10,000 iterations we get testDigestNew 165 ms
     * at 100,000 iterations we got testDigestNew 544 ms
     * at 1,000,000 iterations we got testDigestNew 3388 ms
     * @throws Exception 
     */
    @Test
    public void testDigestNew() throws Exception {
        long start = System.currentTimeMillis();
        for(int i=0; i<max; i++) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plaintext);
        }
        long stop = System.currentTimeMillis();
        log.debug("testDigestNew {} ms", stop-start);
    }
    
    /***
     * at 1,000 iterations we get testDigestCache 14 ms     65% faster
     * at 10,000 iterations we get testDigestCache 50 ms    70% faster
     * at 100,000 iterations we got testDigestCache 301 ms  45% faster 
     * at 1,000,000 iterations we got testDigestCache 2917 ms  15% faster
     * 
     * @throws Exception 
     */
    @Test
    public void testDigestCache() throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        long start = System.currentTimeMillis();
        for(int i=0; i<max; i++) {
            byte[] digest = md.digest(plaintext);
        }
        long stop = System.currentTimeMillis();
        log.debug("testDigestCache {} ms", stop-start);
    }
    
}
