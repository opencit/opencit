/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.crypto;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class AesKeyTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AesKeyTest.class);

    @Test
    public void testGenerateKeyAes128() {
        log.debug("128-bit key: {}", RandomUtil.randomBase64String(16));
    }
}
