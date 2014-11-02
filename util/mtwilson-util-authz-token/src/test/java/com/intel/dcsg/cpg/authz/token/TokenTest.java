/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.authz.token;

import com.intel.dcsg.cpg.crypto.key.HashMapMutableSecretKeyRepository;
import com.intel.dcsg.cpg.crypto.Aes;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.key.KeyNotFoundException;
import com.intel.dcsg.cpg.crypto.key.MessageIntegrityException;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.performance.AlarmClock;
import com.intel.dcsg.cpg.io.ByteArray;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author jbuhacoff
 */
public class TokenTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenTest.class);
    
    /**
     * Sample output from previous implementation:
     * 
2013-12-05 09:19:13,510 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:37] Creating token
2013-12-05 09:19:14,443 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:45] Token version: 1
2013-12-05 09:19:14,444 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:46] Token key id: 2b22bbd5-6ef3-4257-a0e0-d4707615cc2d
2013-12-05 09:19:14,449 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:50] Token length in bytes: 257
2013-12-05 09:19:14,450 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:51] Token base64: ASsiu9Vu80JXoODUcHYVzC23Aa6GTSURqBjBYVb/jmzx+w+n2eILkzbaFAPxSp9/Yl/IkfeD1FZJmxfd1xFzUSeE7kKZ0dwxYPDqm9a25BWc3+WZfBRdFIEsKW2CdIxpnOqGaW9fev7tZf6tmJimGPipOHOrtLftGqpjD7KrXwaoD619aOt2p1JB9ehWeVENwGfd5k7Er6KDqiey+RvSJpL1q2rOXRmGfVXQVbDMB+gYrFn4bnrPnZ83H/mDSrJAR+4va9JlxzAL9U7zQ7naaoIoF2OCejbIzraYWCgYtDJdhscJ+xCRdLmus0gxavMDGXcyB5xeJJObKujW9Uru82k=
2013-12-05 09:19:14,453 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:58] Reading existing token
2013-12-05 09:19:14,459 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:60] Version: 1
2013-12-05 09:19:14,459 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:61] Key ID: 2b22bbd5-6ef3-4257-a0e0-d4707615cc2d
2013-12-05 09:19:14,460 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:62] Nonce: 97e697baf851f27d86c5520c4edc653391634e673680d63b30ddaea8b2829a764fbf6cb5c8330fcc6c9a54987126ffa35320515414745bf96406b2e1f5ddf83fff41902c142fb36057e3391014b9253d707bee822e7afc45bb1f45acb431b77af42745ad114f6c95d4d3426cc7d2cd1a5594e1298f7cc0fe1db1a04ff3676821c3929947c59193487ca868c23b6de53229ba3f8cd2de205f53e60770a66bf3ad201af954f4718a47
2013-12-05 09:19:14,461 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:63] Timestamp: 1386263954  (was 0 seconds ago)
2013-12-05 09:19:14,461 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:64] Token value: test-userid
     * 
     * Sample output from current implementation:
     * 
     * 
     * @throws CryptographyException
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
    @Test
    public void testCreateToken() throws Exception {
        log.debug("Creating token");
        UUID userUuid = new UUID();
        String userId = userUuid.toString(); // with hyphens
        
        TokenFactory factory = new TokenFactory();
        String token = factory.create(userId);
        log.debug("Token user id: {}", userId);
        log.debug("Token base64: {}", token);
                
        // validate
        log.debug("Reading existing token");
        TokenValidator validator = new TokenValidator(factory);
        Token confirm = validator.validate(token);
        log.debug("Version: {}", confirm.getVersion());
        log.debug("Nonce: {}", Hex.encodeHexString(confirm.getNonce()));
        log.debug("Timestamp: {}  (was {} seconds ago)", confirm.getTimestamp(), System.currentTimeMillis()/1000L - confirm.getTimestamp());
        log.debug("User id from token value: {}", new String(confirm.getContent(), Charset.forName("UTF-8"))); // in bytes it would be [116, 101, 115, 116, 45, 117, 115, 101, 114, 105, 100]
    }
    
    /**
     * This test confirms that changing any byte in the token will cause it to fail validation
     * 
     * Example output from previous version:
     * 
2013-12-05 09:31:38,665 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:97] Token under test: ARAcIFhJFUZyg7PDmBuPK5g5NLWCpJrZ0gkViyyg3iRw4+iTWxqUmIWjLntEXO5oh8Iheq0NWBKIx5IycrcSJqBMZpV5f8l8m54p8td2V+I3lCaX5rJSomtTFhV1gr08hN0xperE1UAl+7QnPSh9f1MpI/dDYOEjTmcTBomeenROey1tzMFLfEmP3USrdby17hhHw0YiMoAJRy6ntaVKBt5V2fXqWmEH/FpEJXdQN3QikHOsbR1sx41Cs1j5ETbtS7oCtU3uFGS75IGFbTe7JxDfVTt1hNjGhH8N6vU+e1sGBIBiSliDLCqN
2013-12-05 09:31:38,669 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:104] Corrupting byte index 0
2013-12-05 09:31:38,671 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:104] Corrupting byte index 1
[ ... snip ... ]
2013-12-05 09:31:38,848 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:104] Corrupting byte index 232
2013-12-05 09:31:38,849 DEBUG [main] c.i.d.c.a.t.Rfc822TokenTest [Rfc822TokenTest.java:104] Corrupting byte index 233
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.809 sec
     * 
     * Also this shows that you can validate more than 250 tokens in under a second, which is acceptable performance for the UI (casual users not APIs)
     * 
     * Example output from current version:
     * 
     * 
     */
    @Test
    public void testDetectCorruptToken() throws Exception {
        log.debug("Creating token");
        UUID userUuid = new UUID();
        String userId = userUuid.toString(); // with hyphens
        TokenFactory factory = new TokenFactory();
        String token = factory.create(userId);
        log.debug("Token under test: {}", token);
        
        byte[] tokenBytes = Base64.decodeBase64(token);
        
        TokenValidator validator = new TokenValidator(factory);
        // this loop changes one byte of the token at a time (always starting from the original token) and validates it to look for the error
        for(int i=0; i<tokenBytes.length; i++) {
            byte[] test = ByteArray.concat(tokenBytes); // make a copy of the token for this test
            // change one byte of the token by setting it to zero, unless it was already zero and then we flip it to 255
            if( test[i] == (byte)0 ) { test[i] = (byte)255; } else { test[i] = (byte)0; }
            try {
                log.debug("Corrupting byte index {}", i);
                        Token confirm = validator.validate(test);
                        fail(); // constructor must throw exception because the token under test is invalid. so if we get here the test failed
            }
            catch(UnsupportedTokenVersionException e) {
                // good, token failed validation -  0 in first byte is unsupported version
            }
            catch(GeneralSecurityException e) {
                if( e.getCause() != null && e.getCause() instanceof MessageIntegrityException ) {
                    // good,  token failed validation due to corrupted key id 
                }
                else {
                    // token failed validation for some other reason we didn't expect, so fail the test
                    throw e;
                }
            }
            catch(IllegalArgumentException e) {
                // good, token failed validation
            }
            catch(Exception e) {
                // any other exception may indicate a problem with our test
                log.error("Cannot validate token", e);
                throw e;
            }
        }
        
    }
    
    @Test 
    public void testInvalidVersion() throws Exception {
        log.debug("Creating token");
        UUID userUuid = new UUID();
        String userId = userUuid.toString(); // with hyphens
        TokenFactory factory = new TokenFactory();
        String token = factory.create(userId);
        log.debug("Token under test: {}", token);

        // change version byte to 2
        byte[] tokenBytes = Base64.decodeBase64(token);
        tokenBytes[0] = (byte)2;
        
        // validate, look for UnsupportedTokenVersionException
        TokenValidator validator = new TokenValidator(factory);
        try {
        Token confirm = validator.validate(tokenBytes);
        fail(); // should not get here
        }
        catch(UnsupportedTokenVersionException e) {
            // good 
        }
    }
    
    
    @Test 
    public void testExpiredToken() throws Exception {
        log.debug("Creating token");
        UUID userUuid = new UUID();
        String userId = userUuid.toString(); // with hyphens
        TokenFactory factory = new TokenFactory();
        String token = factory.create(userId);
        log.debug("Token under test: {}", token);

        // wait 2 seconds
        AlarmClock clock = new AlarmClock(2, TimeUnit.SECONDS);
        clock.sleep();
        
        // validate, look for UnsupportedTokenVersionException
        TokenValidator validator = new TokenValidator(factory);
        validator.setExpiresAfter(1); // second
        try {
        Token confirm = validator.validate(token);
        fail(); // should not get here
        }
        catch(ExpiredTokenException e) {
            // good 
        }
    }
    
}
