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
public class Sha1DigestTest {
    
    @Test(expected=IllegalArgumentException.class)
    public void testSha1NullByteArrayConstructor() {
        Sha1Digest sha1 = new Sha1Digest((byte[])null);
        System.out.println(sha1.toString());        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSha1NullStringConstructor() {
        Sha1Digest sha1 = new Sha1Digest((String)null);
        System.out.println(sha1.toString());        
    }

    @Test
    public void testSha1NullByteArrayValueOf() {
        Sha1Digest sha1 = Sha1Digest.valueOf((byte[])null);
        assertNull(sha1);
        System.out.println(String.valueOf(sha1));        
    }
    
    @Test
    public void testSha1NullStringValueOf() {
        Sha1Digest sha1 = Sha1Digest.valueOf((String)null);
        assertNull(sha1);
        System.out.println(String.valueOf(sha1));        
    }
    
    @Test
    public void testSha1plain() {
        String hex = "0000000000000000000000000000000000000000";
        Sha1Digest sha1 = new Sha1Digest(hex);
        System.out.println(sha1.toString());
    }
    
    @Test
    public void testSha1WithColons() {
        String hex = "00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00";
        Sha1Digest sha1 = new Sha1Digest(hex);
        System.out.println(sha1.toString());
    }

    @Test
    public void testSha1WithSpaces() {
        String hex = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        Sha1Digest sha1 = new Sha1Digest(hex);
        System.out.println(sha1.toString());
    }
}
