/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.digest;

import com.intel.mtwilson.model.Sha1Digest;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class HashTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Test
    public void testUpdateUpdateSameAsLongerMessage() throws NoSuchAlgorithmException {
        Sha1Digest a = new Sha1Digest("0000000000000000000000000000000000000000");
        Sha1Digest b = new Sha1Digest("0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f");
        Sha1Digest c = new Sha1Digest("aabbccddeeff00112233445566778899aabbccdd");
        MessageDigest hash = MessageDigest.getInstance(a.algorithm());
        hash.update(a.toByteArray());
        hash.update(b.toByteArray());
        Sha1Digest r = new Sha1Digest(hash.digest());
        log.debug("a.extend(b) = "+r.toString());
        hash.reset();
        byte[] cat = new byte[40];
        System.arraycopy(a.toByteArray(), 0, cat, 0, 20);
        System.arraycopy(b.toByteArray(), 0, cat, 20, 20);
        hash.update(cat);
        Sha1Digest r2 = new Sha1Digest(hash.digest());        
        log.debug("a||b = "+r2.toString());
    }
    
    @Test
    public void testExtendZeros() {
        Sha1Digest a = new Sha1Digest("0000000000000000000000000000000000000000");
        Sha1Digest b = new Sha1Digest("0de3710ee2f658a382f2531213233024175a63dd");
        Sha1Digest r = a.extend(b.toByteArray());
        log.debug(r.toString()); // b should be: 8990812e31357a6266a86c079ff49d61b442335b
        assertEquals("8990812e31357a6266a86c079ff49d61b442335b", r.toString());
    }
    
    @Test
    public void testSerializeSha1Digest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        log.debug(mapper.writeValueAsString(new Sha1Digest("0de3710ee2f658a382f2531213233024175a63dd")));
    }
}
