/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto;

import com.intel.dcsg.cpg.performance.*;
import com.intel.dcsg.cpg.performance.report.*;
import com.intel.mtwilson.crypto.Aes128;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.map.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO:
 * 1. single-threaded, single-host performance test
 * 1. single-threaded, multiple-host performance test
 * 1. multi-threaded, single-host performance test
 * 1. multi-threaded, multiple-host performance test
 * X. utility class for loading environment  ---  already in My.environment() ?  
 * X. utility functions for picking out hosts in the environment according to characteristics... and possibly
 *    a data file to support that.  for example "i need two vmware hosts with the same MLE"  or maybe some 
 *    functions to detect these required combinations within a defined environment and then make available what
 *    we find...
 * @author jbuhacoff
 */
public class CipherConcurrencyTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    private int howManyTimes = 75; // when testing a single task multiple times, do it this number of times (sample size)
    private static Aes128 cipher;
    private long timeout = 15;
    private ObjectMapper mapper = new ObjectMapper();
    
    @BeforeClass
    public static void initCipher() throws CryptographyException {
        cipher = new Aes128(Aes128.generateKey());
    }
    
    private static class DecryptTask implements Runnable {
        private Logger log = LoggerFactory.getLogger(getClass());
        private String ciphertext = null;
        private String plaintext = null;
        public DecryptTask() {
            try {
                plaintext = Base64.encodeBase64String(Aes128.generateKey().getEncoded()); // generate random string
                ciphertext = cipher.encryptString(plaintext); // prepare an encryption of it
                if( !plaintext.equals(cipher.decryptString(ciphertext))) {
                    throw new IllegalArgumentException("Bad test: failed to decrypt generated ciphertext");
                }
            }
            catch(Exception e) {
                throw new IllegalArgumentException("Cannot prepare DecryptTask: "+e.toString(), e);
            }
        }
        @Override
        public void run() {
            try {
                String decrypted = cipher.decryptString(ciphertext);
                log.debug("Decrypted text: {}", decrypted);
                if( !plaintext.equals(decrypted)) {
                    throw new IllegalArgumentException("Bad decryption of "+plaintext+": "+decrypted);
                }
            }
            catch(CryptographyException e) {
                throw new IllegalArgumentException("Cannot decrypt text: "+e.toString(), e);
            }
        }
        public String getPlaintext() { return plaintext; }
    }
    
    
    @Test
    public void testConcurrentDecrypt() throws Exception {
        ArrayList<Task> tasks = new ArrayList<Task>();
        for(int i=0; i<howManyTimes; i++) {
            tasks.add(new Task(new DecryptTask())); // construct the objects one at a time 
        }
        PerformanceInfo info = PerformanceUtil.measureMultipleConcurrentTasks(tasks, timeout);
        long[] data = info.getData();
        log.debug("samples: {}", data.length);
        log.debug("min: {}", info.getMin());
        log.debug("max: {}", info.getMax());
        log.debug("avg: {}", info.getAverage());
        log.debug("performance info: {}", mapper.writeValueAsString(info));
        for(int i=0; i<howManyTimes; i++) {
            Task task = tasks.get(i);
            if( task.isError() ){
                log.debug("Error: {}", task.getCause().toString());
            }
        }
    }
}
