/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;

import com.intel.dcsg.cpg.io.pem.InternetMessage;
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
public class InternetMessageTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Sample output:
QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=
     */
    @Test
    public void testCreatePemFormatNoHeaders() {
        // create random content for the output
        byte[] content = new byte[32];
        Random rnd = new Random();
        rnd.nextBytes(content);
        InternetMessage message = new InternetMessage(Base64.encodeBase64String(content));
        log.debug("message:\n"+message.toString());
    }

    /**
     * Sample output:
QEStdcdTFsENMiRgrUgvP1S9ZbKmp0zXP6NbzDd9iN+cROuKMHOqY0AnwCQ6Q1r9nZMUN8owhk63
fK6M0VPKA6TjFVDmX7VGKb0Sk8rfIddBV3TKZKPerYwl3VSv7iut9QZC7zXiZHgZVKJiBCpsoSql
wQxBTlElZg+ho8yY4G0=
     */
    @Test
    public void testCreatePemFormatChunked() {
        // create random content for the output
        byte[] content = new byte[128];
        Random rnd = new Random();
        rnd.nextBytes(content);
        InternetMessage message = new InternetMessage(new String(Base64.encodeBase64Chunked(content)));
        log.debug("message:\n"+message.toString());
    }
    
    
    
    /**
     * Sample data:
blank-attr2: 
attr1: value1

1Jz33w6EWlV15Hj/wG06XT1LdR8oiyV3orM+UwLno8s=
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
        InternetMessage message = new InternetMessage(Base64.encodeBase64String(content));
        log.debug("message:\n"+message.toString());
    }
    
    @Test
    public void testParseMessageFormatNoHeaders() {
        String input = "" +
"QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n";
        InternetMessage message = InternetMessage.valueOf(input);
        assertEquals("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=", message.getContent());
        assertTrue(message.getHeaders().isEmpty());
    }

    @Test
    public void testParsePemFormatWithHeaders() {
        String input = "" +
"attr2: value2\n"+
"attr1: value1\n"+
"\n"+
"QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n";
        InternetMessage message = InternetMessage.valueOf(input);
        assertEquals("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=", message.getContent());
        assertEquals(2,message.getHeaders().size());
        assertTrue(message.getHeaders().containsKey("attr1"));
        assertEquals("value1", message.getHeaders().get("attr1"));
        assertTrue(message.getHeaders().containsKey("attr2"));
        assertEquals("value2", message.getHeaders().get("attr2"));
    }
    

    @Test
    public void testParsePemFormatWithHeadersHavingOneBlankValue() {
        String input = "" +
"attr2: \n"+
"attr1: value1\n"+
"\n"+
"QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=\n";
        InternetMessage message = InternetMessage.valueOf(input);
        assertEquals("QmB5gMsc8ufk9lmcwVHBXHMV50bzLtKD/r+3nn1H1+o=", message.getContent());
        assertEquals(2,message.getHeaders().size());
        assertTrue(message.getHeaders().containsKey("attr1"));
        assertEquals("value1", message.getHeaders().get("attr1"));
        assertTrue(message.getHeaders().containsKey("attr2"));
        assertNull(message.getHeaders().get("attr2"));
    }

}
