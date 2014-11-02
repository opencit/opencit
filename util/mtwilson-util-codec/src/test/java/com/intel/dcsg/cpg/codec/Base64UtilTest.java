/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.codec;

import com.intel.mtwilson.codec.Base64Util;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
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
    public void testTrim() {
        assertEquals("abcd", Base64Util.trim("a\n b\t c\r d"));
        assertTrue(Base64Util.isBase64(Base64Util.trim("6 MW aL7\nlVS\rDiqp\njVgmH\r\nhmxGq\t\t9f2BMSrD \t gv7yLpm \n OIyH \r Y=")));
    }
    
    @Test
    public void testLengths() {
        assertEquals(44, Math.round(4*Math.ceil(1.0*32/3)));
    }
    
    @Test
    public void testDecodeInvalidBase64() {
        byte[] result = Base64.decodeBase64("test. invalid$ base*64 string="); // can also omit final equal sign or have two of them, result will be the same
        String result2 = Base64.encodeBase64String(result); // testinvalidbase64string=
        log.debug("result is {}", result2);
        String normalized = Base64Util.normalize("test. invalid$ base*64 string=");
        assertEquals(normalized, result2); // only works in some cases;  for example if the original string didn't have the single trailing equal sign this would not be equal to the result
        
    }
    
    @Test
    public void testNonPrintable() {
        Pattern nonprintable = Pattern.compile("[^\\p{Print}]");
        assertFalse(nonprintable.matcher("aaaa").matches());
        assertEquals("aaaa", "aaaa\u0001".replaceAll("[^\\p{Print}]", ""));
    }
}
