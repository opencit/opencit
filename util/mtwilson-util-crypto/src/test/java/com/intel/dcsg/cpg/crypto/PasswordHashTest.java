/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.crypto.PasswordHash;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class PasswordHashTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testHashPassword() throws CryptographyException {
        String input = "password";
        PasswordHash password = new PasswordHash(input);
        String output = password.getHashBase64();
        String salt = password.getSaltBase64();
        log.info("Input: {}, Output: {}, Salt: {}", input, output, salt );
        assertEquals(8, password.getSalt().length); // because it uses 8-byte salts
        assertEquals(32, password.getHash().length); // because it uses sha256
        
        // now create a new password hash using the same salt to make sure it's the same
        PasswordHash password2 = new PasswordHash(input, salt);
        assertEquals(salt, password2.getSaltBase64());
        assertEquals(output, password2.getHashBase64());
    }
    
    @Test
    public void testHashPasswordNoSalt() throws CryptographyException {
        String input = "password";
        PasswordHash password = new PasswordHash(input, new byte[0]);
        String output = password.getHashBase64();
        String salt = password.getSaltBase64();
        log.info("Input: {}, Output: {}, Salt: {}",input, output, salt );
        assertEquals(0, password.getSalt().length); // because it uses 8-byte salts
        assertEquals(32, password.getHash().length); // because it uses sha256
    }
    
    @Test
    public void testHashPasswordIsEqualToInputPassword() throws CryptographyException {
        PasswordHash password = new PasswordHash("existing password"); // hash a password with random salt
        assertTrue(password.isEqualTo("existing password"));
        assertFalse(password.isEqualTo("wrong password"));
    }
}
