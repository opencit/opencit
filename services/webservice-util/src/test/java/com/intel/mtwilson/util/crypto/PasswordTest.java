/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto;

import com.intel.mtwilson.crypto.Password;
import com.intel.mtwilson.crypto.CryptographyException;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class PasswordTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testHashPassword() throws CryptographyException {
        String input = "password";
        Password password = new Password(input);
        String output = password.getHashBase64();
        String salt = password.getSaltBase64();
        log.debug("Input: {}, Output: {}, Salt: {}",  input, output, salt);
        assertEquals(8, password.getSalt().length); // because it uses 8-byte salts
        assertEquals(32, password.getHash().length); // because it uses sha256
        
        // now create a new password hash using the same salt to make sure it's the same
        Password password2 = new Password(input, salt);
        assertEquals(salt, password2.getSaltBase64());
        assertEquals(output, password2.getHashBase64());
    }
}
