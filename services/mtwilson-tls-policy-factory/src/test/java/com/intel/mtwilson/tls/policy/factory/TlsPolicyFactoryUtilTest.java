/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyFactoryUtilTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyFactoryUtilTest.class);

    @Test
    public void testGuessEncodingFromSampleDataHex() {
        String data = "?b0 91 3c 79 2d 67 19 84 64 ea c9 2a d9 03 44 3f 4b 8c be 0f";  // this punctuation is pinrintable so doesn't get stripped ... and detection fails...
//        String data = "‎1a b9 06 ec 0f ae dc 65 d2 3e 86 18 69 2a 33 4a 53 14 51 ec"; // there's a hidden nonprintable character in front of the first hex byte 1a ; this gets stripped off correctly and the test passes
        String encoding = TlsPolicyFactoryUtil.guessEncodingForData(data);
        log.debug("encoding: {}", encoding);
        assertEquals("hex",encoding);
    }

    @Test
    public void testGuessEncodingFromSampleDataBase64() {
        String data = "sJE8eS1nGYRk6skq2QNEP0uMvg8="; 
//        String data = "?b0 91 3c 79 2d 67 19 84 64 ea c9 2a d9 03 44 3f 4b 8c be 0f";  // this punctuation is pinrintable so doesn't get stripped ... and detection fails...
//        String data = "‎1a b9 06 ec 0f ae dc 65 d2 3e 86 18 69 2a 33 4a 53 14 51 ec"; // there's a hidden nonprintable character in front of the first hex byte 1a ; this gets stripped off correctly and the test passes
        String encoding = TlsPolicyFactoryUtil.guessEncodingForData(data);
        log.debug("encoding: {}", encoding);
        assertEquals("base64",encoding);
    }

}
