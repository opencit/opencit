/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.io.Base64Util;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class Base64UtilTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testValidation() {
        assertTrue(Base64Util.isBase64("i/O1W4YJ9ZQsgocvNc2IIsg3xb3lLhvg"));
        assertTrue(Base64Util.isBase64("1zU3pDzK/FE="));
        assertFalse(Base64Util.isBase64("1zU3pDzK/FE==")); // one too many = at the end
        assertTrue(Base64Util.isBase64("6MWaL7lVSDiqpjVgmHhmxGq9f2BMSrDgv7yLpmOIyHY="));
        assertFalse(Base64Util.isBase64("1zU3pDzK/FE=:6MWaL7lVSDiqpjVgmHhmxGq9f2BMSrDgv7yLpmOIyHY=")); // should fail because of the colon and equals sign immediately before the colon
    }
    
    @Test
    public void testLengths() {
        assertEquals(44, Math.round(4*Math.ceil(1.0*32/3)));
    }
}
