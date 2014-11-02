/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.digest;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class DigestReadingTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigestReadingTest.class);

    @Test
    public void testDecodeSha1() {
        String alg = "SHA1";
        Digest digestNormal = new Digest(alg, Base64.decodeBase64("189ae6e0266fae638f8c9cb092e1ad04c3a758ab"));
        Digest digestColons = new Digest(alg, Base64.decodeBase64("18:9a:e6:e0:26:6f:ae:63:8f:8c:9c:b0:92:e1:ad:04:c3:a7:58:ab"));
        Digest digestSpaces = new Digest(alg, Base64.decodeBase64("18 9a e6 e0 26 6f ae 63 8f 8c 9c b0 92 e1 ad 04 c3 a7 58 ab"));
        assertArrayEquals(digestNormal.getBytes(), digestColons.getBytes());
        assertArrayEquals(digestNormal.getBytes(), digestSpaces.getBytes());
    }
}
