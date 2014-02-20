/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.codec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class CodecTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CodecTest.class);

    @Test
    public void testBase64Decode() throws UnsupportedEncodingException {
        String base64 = Base64.encodeBase64String("hello, world".getBytes("UTF-8"));
        byte[] decoded = Base64.decodeBase64(base64);
        log.debug("auth: {}", new String(decoded, Charset.forName("UTF-8")));
    }
}
