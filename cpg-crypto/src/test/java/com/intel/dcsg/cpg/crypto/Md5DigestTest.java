/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class Md5DigestTest {
    @Test
    public void testMd5plain() {
        String hex = "00000000000000000000000000000000";
        Md5Digest md5 = new Md5Digest(hex);
        System.out.println(md5.toString());
    }
    
    @Test
    public void testMd5WithColons() {
        String hex = "00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00";
        Md5Digest md5 = new Md5Digest(hex);
        System.out.println(md5.toString());
    }

    @Test
    public void testMd5WithSpaces() {
        String hex = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        Md5Digest md5 = new Md5Digest(hex);
        System.out.println(md5.toString());
    }
}
