/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto.password;

import com.intel.dcsg.cpg.crypto.key.password.Password;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class PasswordTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordTest.class);

    @Test
    public void testZeroizePassword() {
        Password password = new Password("test".toCharArray());
        log.debug("password before clear: {}", password.toCharArray());
        password.clear();
        log.debug("password after clear: {}", password.toCharArray());
        
    }
}
