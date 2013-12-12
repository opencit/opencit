/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.io.ByteArray;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class Sha1DigestTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Sha1DigestTest.class);
    
    @Test(expected=IllegalArgumentException.class)
    public void testSha1NullByteArrayConstructor() {
        Sha1Digest sha1 = new Sha1Digest((byte[])null);
        log.debug(sha1.toString());        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSha1NullStringConstructor() {
        Sha1Digest sha1 = new Sha1Digest((String)null);
        log.debug(sha1.toString());        
    }

    @Test
    public void testSha1NullByteArrayValueOf() {
        Sha1Digest sha1 = Sha1Digest.valueOf((byte[])null);
        assertNull(sha1);
        log.debug(String.valueOf(sha1));        
    }
    
    @Test
    public void testSha1NullStringValueOf() {
        Sha1Digest sha1 = Sha1Digest.valueOf((String)null);
        assertNull(sha1);
        log.debug(String.valueOf(sha1));        
    }
    
    @Test
    public void testSha1plain() {
        String hex = "0000000000000000000000000000000000000000";
        Sha1Digest sha1 = new Sha1Digest(hex);
        log.debug(sha1.toString());
    }
    
    @Test
    public void testSha1WithColons() {
        String hex = "00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00";
        Sha1Digest sha1 = new Sha1Digest(hex);
        log.debug(sha1.toString());
    }

    @Test
    public void testSha1WithSpaces() {
        String hex = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        Sha1Digest sha1 = new Sha1Digest(hex);
        log.debug(sha1.toString());
    }
    
    @Test
    public void testExtendHash() {
        Sha1Digest tag = Sha1Digest.valueOfHex("de8990b384d71983a7646e65326a699acf463d3c");
        Sha1Digest extend1 = Sha1Digest.ZERO.extend(tag.toByteArray());
        Sha1Digest extend2 = Sha1Digest.digestOf(ByteArray.concat(Sha1Digest.ZERO.toByteArray(), tag.toByteArray()));
        assertEquals(extend1.toHexString(), extend2.toHexString());
    }

    @Test
    public void testExtendHashCitrix() throws Exception {
        Sha1Digest tag = Sha1Digest.digestOf("2fd92b1db428f1e7b3f2d826c5c7677efd024f69".getBytes("UTF-8"));// citrix stores the asset tag in nvram as a utf-8 character string instead of the real value they represent
        Sha1Digest extend1 = Sha1Digest.ZERO.extend(tag.toByteArray());
        Sha1Digest extend2 = Sha1Digest.digestOf(ByteArray.concat(Sha1Digest.ZERO.toByteArray(), tag.toByteArray()));
        assertEquals(extend1.toHexString(), extend2.toHexString());
        log.debug(extend1.toHexString());
    }
    
    @Test
    public void testXenTag() {
        Sha1Digest tag = Sha1Digest.valueOfHex("de8990b384d71983a7646e65326a699acf463d3c");
        Sha1Digest tag_sha1 = Sha1Digest.digestOf(tag.toByteArray());
        Sha1Digest tag_sha1_sha1 = Sha1Digest.digestOf(tag_sha1.toByteArray());
        Sha1Digest geotag_sha1 = Sha1Digest.ZERO.extend(tag_sha1_sha1.toByteArray());
        log.debug(geotag_sha1.toHexString()); // b5f87bfff064b8646882dce8d4614eefbbbbf83b  but pcr22 shows D0DA0B51DC5253D468DA371A4D16322BBBCEAD1E
        Sha1Digest geotag_sha1_sha1 = Sha1Digest.digestOf(geotag_sha1.toByteArray());
        log.debug(geotag_sha1_sha1.toHexString()); // 36e4a729d55a3e8a53dddebf4d17c6281815d850  , still wrong
        Sha1Digest tag_sha1_sha1x = Sha1Digest.digestOf(tag_sha1_sha1.toByteArray());
        Sha1Digest geotag_sha1x = Sha1Digest.ZERO.extend(tag_sha1_sha1x.toByteArray());
        log.debug(geotag_sha1x.toHexString()); // 65d90db6f003286c407c829298eff93a5aeb94ec,  still wrong
    }
    
    @Test
    public void testSha1Extend() {
        Sha1Digest extended = Sha1Digest.ZERO.extend(Sha1Digest.ZERO); // b80de5d138758541c5f05265ad144ab9fa86d1db
        log.debug(extended.toHexString());
        Sha1Digest extended2 = Sha1Digest.ZERO.extend(Sha1Digest.digestOf(Sha1Digest.ZERO.toByteArray())); // 2c754ca949cfa83b323df8ed0057333551d15dc2
        log.debug(extended2.toHexString());
        Sha1Digest extended3 = Sha1Digest.ZERO.extend(Sha1Digest.valueOfHex("de8990b384d71983a7646e65326a699acf463d3c")); // 27b8c2054f13825c2433b28b45e20fa6c02a7ca5
        log.debug(extended3.toHexString());
        Sha1Digest extended4 = Sha1Digest.ZERO.extend(Sha1Digest.digestOf(Sha1Digest.valueOfHex("de8990b384d71983a7646e65326a699acf463d3c").toByteArray()));// a2dd2238a3d35a71d21c53e223e568faf2f082c0
        log.debug(extended4.toHexString());
        Sha1Digest extended4b = Sha1Digest.ZERO.extend(Sha1Digest.digestOf(Sha1Digest.digestOf(Sha1Digest.valueOfHex("de8990b384d71983a7646e65326a699acf463d3c").toByteArray()).toByteArray()));// a2dd2238a3d35a71d21c53e223e568faf2f082c0
        log.debug(extended4b.toHexString());
        byte[] memory = new byte[16384];   // PAGE_SIZE = 4096 = 2^12   which is 1<<12 (PAGE_SHIFT=12)    ,   uint32_t array[PAGE_SIZE] would be 16384 = 4 * 4096
        for(int i=0; i<memory.length; i++) { memory[i] = 0; }
        Sha1Digest tag = Sha1Digest.valueOfHex("de8990b384d71983a7646e65326a699acf463d3c");
        System.arraycopy(tag.toByteArray(), 0, memory, 0, 20);
        Sha1Digest memorySha1 = Sha1Digest.digestOf(memory);
        Sha1Digest extended5 = Sha1Digest.ZERO.extend(memorySha1); // 5da248ef80598121afcf739d64c224b7d215f552 if memory is 4096 bytes, d6da450f13bef754c118f85ceb6128287054fe5e if memory is 16384 bytes
        log.debug(extended5.toHexString());
        Sha1Digest extended6 = Sha1Digest.ZERO.extend(Sha1Digest.digestOf(memorySha1.toByteArray())); // 2fe3b71980d9690fee6855ac94a741ce0a3133d0 if memory is 4096 bytes, f3ed8c986e1567817964b352a33dcc38c620 if memory is 16384 bytes
        log.debug(extended6.toHexString());
    }
    
    @Test
    public void testEquals() {
        assertEquals(Sha1Digest.valueOfHex("de8990b384d71983a7646e65326a699acf463d3c"), Sha1Digest.valueOfHex("de8990b384d71983a7646e65326a699acf463d3c"));
    }
    
    @Test
    public void testFindHash() {
        Sha1Digest tag = Sha1Digest.valueOfHex("de8990b384d71983a7646e65326a699acf463d3c");
//        Sha1Digest tag = Sha1Digest.ZERO; //valueOfHex("de8990b384d71983a7646e65326a699acf463d3c");
        Sha1Digest target = Sha1Digest.valueOfHex("D0DA0B51DC5253D468DA371A4D16322BBBCEAD1E");
        Sha1Digest result;
        int size;
        byte[] memory;
        for(size = 20; size<=16384; size++) {
            if( size % 16 == 0 ) { log.debug("size {}", size); }
            memory = new byte[size];
            for(int i=0; i<memory.length; i++) { memory[i] = 0; }
            System.arraycopy(tag.toByteArray(), 0, memory, 0, 20);
            Sha1Digest memoryDigest = Sha1Digest.digestOf(memory);
            Sha1Digest hashed1 = Sha1Digest.digestOf(memoryDigest.toByteArray());
            if( hashed1.equals(target)) { log.debug("GOT hashed1 at size {}", size); break; }
            Sha1Digest extended1 = Sha1Digest.ZERO.extend(memoryDigest);
            if( extended1.equals(target)) { log.debug("GOT extended1 at size {}", size); break; }
            Sha1Digest extended2 = Sha1Digest.ZERO.extend(Sha1Digest.digestOf(memoryDigest.toByteArray()));
            if( extended2.equals(target)) { log.debug("GOT extended2 at size {}", size); break; }
        }        
    }
    @Test
    public void testFindHash2() {
        Sha1Digest tag = Sha1Digest.valueOfHex("de8990b384d71983a7646e65326a699acf463d3c");
//        Sha1Digest tag = Sha1Digest.ZERO; //valueOfHex("de8990b384d71983a7646e65326a699acf463d3c");
        Sha1Digest target = Sha1Digest.valueOfHex("D0DA0B51DC5253D468DA371A4D16322BBBCEAD1E");
        Sha1Digest result;
        int size;
        byte[] memory;
        for(size = 20; size<=16384; size++) {
            if( size % 16 == 0 ) { log.debug("size {}", size); }
            memory = new byte[size];
            for(int i=0; i<memory.length; i++) { memory[i] = 0; }
            System.arraycopy(tag.toByteArray(), 0, memory, 0, 20);
            Sha256Digest memoryDigest = Sha256Digest.digestOf(memory);
            Sha1Digest extended3 = Sha1Digest.ZERO.extend(Sha1Digest.digestOf(memoryDigest.toByteArray()));
            if( extended3.equals(target)) { log.debug("GOT extended3 at size {}", size); break; }
        }        
    }
}
