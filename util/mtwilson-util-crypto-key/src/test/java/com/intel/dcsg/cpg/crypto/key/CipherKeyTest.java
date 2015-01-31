/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.util.crypto.key2.Attributes;
import com.intel.mtwilson.util.crypto.key2.CipherKey;
import java.io.IOException;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class CipherKeyTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CipherKeyTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCopyCipherKey() throws JsonProcessingException, IOException {
        CipherKey a = new CipherKey();
        a.setCipherAlgorithm("AES");
        a.setCipherKeyLength(128);
        a.setCipherMode("OFB");
        a.setCipherKeyId("test-key");
        log.debug("cipher key a: {}", mapper.writeValueAsString(a)); // {"cipherAlgorithm":"AES","cipherKeyLength":128,"cipherMode":"OFB","cipherPaddingMode":null,"cipherKeyId":"test-key","encoded":null}
        
        CipherKey b = a.copy();
        log.debug("cipher key b: {}", mapper.writeValueAsString(b)); // {"cipherAlgorithm":"AES","cipherKeyLength":128,"cipherMode":"OFB","cipherPaddingMode":null,"cipherKeyId":"test-key","encoded":null}
        assertEquals(b.getCipherAlgorithm(), a.getCipherAlgorithm());
        assertEquals(b.getCipherKeyLength(), a.getCipherKeyLength());
        assertEquals(b.getCipherMode(), a.getCipherMode());
        assertEquals(b.getCipherKeyId(), a.getCipherKeyId());
        
        // edit b
        b.setCipherKeyId("copy-of-test-key");
        log.debug("edited cipher key b: {}", mapper.writeValueAsString(b)); // {"cipherAlgorithm":"AES","cipherKeyLength":128,"cipherMode":"OFB","cipherPaddingMode":null,"cipherKeyId":"copy-of-test-key","encoded":null}
        log.debug("original cipher key a: {}", mapper.writeValueAsString(a)); // {"cipherAlgorithm":"AES","cipherKeyLength":128,"cipherMode":"OFB","cipherPaddingMode":null,"cipherKeyId":"test-key","encoded":null}
        assertNotEquals(b.getCipherKeyId(), a.getCipherKeyId());
        
        // copy by serializing and include extra attribute
        a.set("createdOn", new Date());
        String json = mapper.writeValueAsString(a);
        CipherKey c = mapper.readValue(json, CipherKey.class);
        log.debug("deserialized cipher key c: {}", mapper.writeValueAsString(c)); // {"cipherAlgorithm":"AES","cipherKeyLength":128,"cipherMode":"OFB","cipherPaddingMode":null,"cipherKeyId":"test-key","encoded":null,"createdOn":1422426510432}
        assertEquals(c.getCipherAlgorithm(), a.getCipherAlgorithm());
        assertEquals(c.getCipherKeyLength(), a.getCipherKeyLength());
        assertEquals(c.getCipherMode(), a.getCipherMode());
        assertEquals(c.getCipherKeyId(), a.getCipherKeyId());
    }
}
