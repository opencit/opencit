/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key.password;

import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class PasswordTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordTest.class);

    @Test
    public void testEmptyPassword() {
        Password empty = new Password();
        assertTrue(empty.isEmpty());
        assertArrayEquals(empty.toCharArray(), new char[0]);
        assertArrayEquals(empty.toByteArray(), new byte[0]);
        assertEquals("", new String(empty.toCharArray()));
    }
}
