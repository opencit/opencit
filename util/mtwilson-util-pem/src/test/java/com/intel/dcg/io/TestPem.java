/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;

import com.intel.dcsg.cpg.io.pem.Pem;
import java.util.HashMap;
import java.util.Random;
import org.apache.commons.codec.binary.Base64;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TestPem {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Sample output:
-----BEGIN RANDOM DATA-----
QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=
-----END RANDOM DATA-----
     */
    @Test
    public void testCreatePemFormatNoHeaders() {
        // create random content for the output
        byte[] content = new byte[32];
        Random rnd = new Random();
        rnd.nextBytes(content);
        Pem pem = new Pem("RANDOM DATA", content, null); // XXX TODO should there be a two-argument constructor for callers taht don't need headers?
        log.debug("pem tostring:\n"+pem.toString());
        log.debug("is pem? {}", Pem.isPem(pem.toString()));
    }

    /**
     * Sample output:
-----BEGIN RANDOM DATA-----
QEStdcdTFsENMiRgrUgvP1S9ZbKmp0zXP6NbzDd9iN+cROuKMHOqY0AnwCQ6Q1r9nZMUN8owhk63
fK6M0VPKA6TjFVDmX7VGKb0Sk8rfIddBV3TKZKPerYwl3VSv7iut9QZC7zXiZHgZVKJiBCpsoSql
wQxBTlElZg+ho8yY4G0=
-----END RANDOM DATA-----
     */
    @Test
    public void testCreatePemFormatChunked() {
        // create random content for the output
        byte[] content = new byte[128];
        Random rnd = new Random();
        rnd.nextBytes(content);
        Pem pem = new Pem("RANDOM DATA", content, null); // XXX TODO should there be a two-argument constructor for callers taht don't need headers?
        log.debug("chunked pem tostring:\n"+pem.toString());
    }
    
    
    
    /**
     * Sample data:
-----BEGIN RANDOM DATA-----
blank-attr2: 
attr1: value1

1Jz33w6EWlV15Hj/wG06XT1LdR8oiyV3orM+UwLno8s=
-----END RANDOM DATA-----     * 
     */
    @Test
    public void testCreatePemFormatWithHeaders() {
        // create random content for the output
        byte[] content = new byte[32];
        Random rnd = new Random();
        rnd.nextBytes(content);
        // create some headers
        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("attr1", "value1");
        headers.put("blank-attr2", "");
        Pem pem = new Pem("RANDOM DATA", content, headers);
        log.debug("pem tostring:\n"+pem.toString());
    }
    
    @Test
    public void testParsePemFormatNoHeaders() {
        String input = "" +
"-----BEGIN RANDOM DATA-----\n"+
"QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n"+
"-----END RANDOM DATA-----";
        Pem pem = Pem.valueOf(input);
        assertEquals("RANDOM DATA", pem.getBanner());
        assertEquals("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=", Base64.encodeBase64String(pem.getContent()));
        assertTrue(pem.getHeaders().isEmpty());
    }

    @Test
    public void testParsePemFormatWithHeaders() {
        String input = "" +
"-----BEGIN RANDOM DATA-----\n"+
"attr2: value2\n"+
"attr1: value1\n"+
"\n"+
"QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n"+
"-----END RANDOM DATA-----";
        Pem pem = Pem.valueOf(input);
        assertEquals("RANDOM DATA", pem.getBanner());
        assertEquals("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=", Base64.encodeBase64String(pem.getContent()));
        assertEquals(2,pem.getHeaders().size());
        assertTrue(pem.getHeaders().containsKey("attr1"));
        assertEquals("value1", pem.getHeaders().get("attr1"));
        assertTrue(pem.getHeaders().containsKey("attr2"));
        assertEquals("value2", pem.getHeaders().get("attr2"));
    }
    

    @Test
    public void testParsePemFormatWithHeadersHavingOneBlankValue() {
        String input = "" +
"-----BEGIN RANDOM DATA-----\n"+
"attr2: \n"+
"attr1: value1\n"+
"\n"+
"QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n"+
"-----END RANDOM DATA-----";
        Pem pem = Pem.valueOf(input);
        assertEquals("RANDOM DATA", pem.getBanner());
        assertEquals("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=", Base64.encodeBase64String(pem.getContent()));
        assertEquals(2,pem.getHeaders().size());
        assertTrue(pem.getHeaders().containsKey("attr1"));
        assertEquals("value1", pem.getHeaders().get("attr1"));
        assertTrue(pem.getHeaders().containsKey("attr2"));
        assertNull(pem.getHeaders().get("attr2"));
    }

}
