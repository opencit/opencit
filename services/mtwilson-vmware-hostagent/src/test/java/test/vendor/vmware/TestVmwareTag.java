/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.vmware;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class TestVmwareTag {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestVmwareTag.class);

    /**
2013-10-24 22:18:33,714 DEBUG [main] t.a.VmwareTest [VmwareTest.java:71] PCR22: 19e63eda3e0830f118b6783817d059e10de1731d
2013-10-24 22:18:33,763 DEBUG [main] t.a.VmwareTest [VmwareTest.java:74] zeroExtendTag -> 19e63eda3e0830f118b6783817d059e10de1731d
     */
    @Test
    public void testVmwareAssetTag() {
        Sha1Digest pcrExpected = Sha1Digest.valueOf(new byte[] { 25, -26, 62, -38, 62, 8, 48, -15, 24, -74, 120, 56, 23, -48, 89, -31, 13, -31, 115, 29 });
        log.debug("PCR22: {}", pcrExpected.toHexString());
        Sha1Digest tag = Sha1Digest.valueOf("fd26b12ba43f666db2dc0ef41d9cc3628ea4c7ed");
        Sha1Digest zeroExtendTag = Sha1Digest.ZERO.extend(tag);
        log.debug("zeroExtendTag -> {}", zeroExtendTag.toHexString()); // 19e63eda3e0830f118b6783817d059e10de1731d        
    }
    
}
