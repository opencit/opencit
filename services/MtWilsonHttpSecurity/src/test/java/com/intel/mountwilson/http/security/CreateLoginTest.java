/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.http.security;

import java.security.SecureRandom;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
/**
 *
 * @author jbuhacoff
 */
public class CreateLoginTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    public CreateLoginTest() {
        
    }
    
    @Test
    public void testGenerateSecretKey() {
        String secretKey = RandomStringUtils.random(248, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ`~!@#$%^&*()-_=+[{]}|;:,./?");
        // client id max length is 128
        // secret key max length is 248
        log.debug("Secret key: "+secretKey);
        assertTrue(true);
//        IOUtils.
    }
    
    @Test
    public void testSecureRandomSecretKey() {
        SecureRandom rnd = new SecureRandom();
        byte[] secretKey = new byte[128];
        rnd.nextBytes(secretKey);
        String secretStr = new String(secretKey);
        assert(secretStr.length() > 0);
    }
    
}
