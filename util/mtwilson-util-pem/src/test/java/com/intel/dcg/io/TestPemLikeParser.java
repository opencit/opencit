/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;

import com.intel.dcsg.cpg.io.pem.PemLikeParser;
import com.intel.dcsg.cpg.io.pem.Pem;
import java.util.HashMap;
import java.util.List;
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
public class TestPemLikeParser {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testParsePemFormatNoHeaders() {
        String input = "" +
"-----BEGIN RANDOM DATA-----\n"+
"QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n"+
"-----END RANDOM DATA-----";
        List<Pem> list = PemLikeParser.parse(input);
        assertEquals(1, list.size());
        Pem pem = list.get(0);
        assertEquals("RANDOM DATA", pem.getBanner());
        assertEquals("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=", new String(pem.getContent())); // Base64.encodeBase64String(pem.getContent()));
        assertTrue(pem.getHeaders().isEmpty());
    }

    @Test
    public void testParsePemFormatOnlyHeaders() {
        String input = "" +
"-----BEGIN HEADER DATA-----\n"+
"Attr1: Value1\n"+
"Attr2: Value2\n"+
"Attr3: \n"+
"-----END HEADER DATA-----";
        List<Pem> list = PemLikeParser.parse(input);
        assertEquals(1, list.size());
        Pem pem = list.get(0);
        assertEquals("HEADER DATA", pem.getBanner());
        assertTrue(new String(pem.getContent()).isEmpty());
        assertEquals("Value1", pem.getHeaders().get("Attr1"));
        assertEquals("Value2", pem.getHeaders().get("Attr2"));
        assertNull(pem.getHeaders().get("Attr3"));
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
        List<Pem> list = PemLikeParser.parse(input);
        assertEquals(1, list.size());
        Pem pem = list.get(0);
        assertEquals("RANDOM DATA", pem.getBanner());
        assertEquals("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=", new String(pem.getContent())); // Base64.encodeBase64String(pem.getContent()));
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
        List<Pem> list = PemLikeParser.parse(input);
        assertEquals(1, list.size());
        Pem pem = list.get(0);
        assertEquals("RANDOM DATA", pem.getBanner());
        assertEquals("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=", new String(pem.getContent())); // Base64.encodeBase64String(pem.getContent()));
        assertEquals(2,pem.getHeaders().size());
        assertTrue(pem.getHeaders().containsKey("attr1"));
        assertEquals("value1", pem.getHeaders().get("attr1"));
        assertTrue(pem.getHeaders().containsKey("attr2"));
        assertNull(pem.getHeaders().get("attr2"));
    }

    @Test
    public void testParsePemFormatWithContinuationHeaders() {
        String input = "" +
"-----BEGIN RANDOM DATA-----\n"+
"attr2: \n"+
"the value for attr2 is on this line\n"+
" and continues on that line\n"+
"attr1: value1, \n"+
"value2, value3\n"+
"\n"+
"QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n"+
"-----END RANDOM DATA-----";
        List<Pem> list = PemLikeParser.parse(input);
        assertEquals(1, list.size());
        Pem pem = list.get(0);
        assertEquals("RANDOM DATA", pem.getBanner());
        assertEquals("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=", new String(pem.getContent())); // Base64.encodeBase64String(pem.getContent()));
        assertEquals(2,pem.getHeaders().size());
        for(String headerName : pem.getHeaders().keySet()) {
            log.debug("Header name: {}", headerName);
            log.debug("Header value: {}", pem.getHeaders().get(headerName));
        }
        assertTrue(pem.getHeaders().containsKey("attr1"));
        assertEquals("value1, value2, value3", pem.getHeaders().get("attr1"));
        assertTrue(pem.getHeaders().containsKey("attr2"));
        assertEquals("the value for attr2 is on this line and continues on that line", pem.getHeaders().get("attr2"));
    }
    
    @Test
    public void testParsePemFormatWithMultipleSections() {
        String input = "" +
"-----BEGIN RANDOM DATA-----\n"+
"document 1\n"+
"-----END RANDOM DATA-----\n"+
"-----BEGIN RANDOM DATA-----\n"+
"document 2\n"+
"-----END RANDOM DATA-----\n"+
"-----BEGIN RANDOM DATA-----\n"+
"document 3\n"+
"-----END RANDOM DATA-----";
        List<Pem> list = PemLikeParser.parse(input);
        assertEquals(3, list.size());
        Pem pem1 = list.get(0);
        assertEquals("RANDOM DATA", pem1.getBanner());
        assertEquals("document 1", new String(pem1.getContent())); // Base64.encodeBase64String(pem.getContent()));
        assertTrue(pem1.getHeaders().isEmpty());
        Pem pem2 = list.get(1);
        assertEquals("RANDOM DATA", pem2.getBanner());
        assertEquals("document 2", new String(pem2.getContent())); // Base64.encodeBase64String(pem.getContent()));
        assertTrue(pem2.getHeaders().isEmpty());
        Pem pem3 = list.get(2);
        assertEquals("RANDOM DATA", pem3.getBanner());
        assertEquals("document 3", new String(pem3.getContent())); // Base64.encodeBase64String(pem.getContent()));
        assertTrue(pem3.getHeaders().isEmpty());
        
    }

    
    @Test
    public void testParsePemFormatWithMultipleSectionsAndOtherContent() {
        String input = "" +
"this is text before the first section\n"+
"-----BEGIN RANDOM DATA-----\n"+
"document 1\n"+
"-----END RANDOM DATA-----\n"+
"this is text between the first and second sections\n"+
"-----BEGIN RANDOM DATA-----\n"+
"document 2\n"+
"-----END RANDOM DATA-----\n"+
"this is text between the second and third sections\n"+
"-----BEGIN RANDOM DATA-----\n"+
"document 3\n"+
"-----END RANDOM DATA-----\n"+
"this is text after the last section\n";
        List<Pem> list = PemLikeParser.parse(input);
        assertEquals(3, list.size());
        Pem pem1 = list.get(0);
        assertEquals("RANDOM DATA", pem1.getBanner());
        assertEquals("document 1", new String(pem1.getContent())); // Base64.encodeBase64String(pem.getContent()));
        assertTrue(pem1.getHeaders().isEmpty());
        Pem pem2 = list.get(1);
        assertEquals("RANDOM DATA", pem2.getBanner());
        assertEquals("document 2", new String(pem2.getContent())); // Base64.encodeBase64String(pem.getContent()));
        assertTrue(pem2.getHeaders().isEmpty());
        Pem pem3 = list.get(2);
        assertEquals("RANDOM DATA", pem3.getBanner());
        assertEquals("document 3", new String(pem3.getContent())); // Base64.encodeBase64String(pem.getContent()));
        assertTrue(pem3.getHeaders().isEmpty());
        
    }
    
}
