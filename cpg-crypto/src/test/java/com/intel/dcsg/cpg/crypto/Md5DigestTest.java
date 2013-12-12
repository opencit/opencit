/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class Md5DigestTest {
    
    @Test(expected=IllegalArgumentException.class)
    public void testMd5NullByteArrayConstructor() {
        Md5Digest md5 = new Md5Digest((byte[])null);
        System.out.println(md5.toString());        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMd5NullStringConstructor() {
        Md5Digest md5 = new Md5Digest((String)null);
        System.out.println(md5.toString());        
    }

    @Test
    public void testMd5NullByteArrayValueOf() {
        Md5Digest md5 = Md5Digest.valueOf((byte[])null);
        assertNull(md5);
        System.out.println(String.valueOf(md5));        
    }
    
    @Test
    public void testMd5NullStringValueOf() {
        Md5Digest md5 = Md5Digest.valueOf((String)null);
        assertNull(md5);
        System.out.println(String.valueOf(md5));        
    }
    
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
