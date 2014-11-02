/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;

import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TestByteArray {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testEmptyConstructor() throws IOException {
        ByteArray array = new ByteArray();
        assertEquals(0, array.length());
        assertNotNull(array.getBytes());
        assertEquals(0, array.getBytes().length);
        assertEquals(BigInteger.ZERO, array.toBigInteger());
    }
    
    @Test
    public void testPreserveLeadingZero() {
        assertEquals(1, ByteArray.fromHex("fa").length());
        assertEquals("fa", ByteArray.fromHex("fa").toHexString());
        assertEquals(1, ByteArray.fromHex("0a").length());
        assertEquals("0a", ByteArray.fromHex("0a").toHexString());
        assertEquals(2, ByteArray.fromHex("00aa").length());
        assertEquals("00aa", ByteArray.fromHex("00aa").toHexString());
        assertEquals(3, ByteArray.fromHex("0000aa").length());
        assertEquals("0000aa", ByteArray.fromHex("0000aa").toHexString());
        assertEquals(4, ByteArray.fromHex("000000aa").length());
        assertEquals("000000aa", ByteArray.fromHex("000000aa").toHexString());
        assertEquals(4, ByteArray.fromHex("00000faa").length());
        assertEquals("00000faa", ByteArray.fromHex("00000faa").toHexString());
    }
    
    @Test
    public void testByteArrayToUUID() {
        // start with a random UUID
        UUID uuid = UUID.randomUUID();
        log.debug("uuid: {}", uuid.toString());
        // convert the UUID to BigInteger
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        BigInteger msbi = BigInteger.valueOf(msb);
        BigInteger lsbi = BigInteger.valueOf(lsb);
        BigInteger uuidi = msbi.add(BigInteger.ONE).shiftLeft(64).add(lsbi); // move msb left 64 bits (8 bytes) to make room for lsb on the right;  adding one first is required, otherwise we always get an off-by-one error like this on the assertion:  f1a57ef26111443[9]a03bb01f893d8e7a  actual: f1a57ef26111443[8]a03bb01f893d8e7a
        // serialize the BigInteger into a ByteArray
        ByteArray array = new ByteArray(uuidi);
        log.debug("Byte array hex: {}", array.toHexString());
        // compare the string representations of the ByteArray and the UUID
        assertEquals(uuid.toString().replace("-", ""), array.toHexString());
        // recreate the UUID using its two long integer components; should be exactly the same
        UUID uuid2 = new UUID(msb, lsb);
        log.debug("uuid2: {}", uuid2.toString());
        assertEquals(uuid, uuid2);
        assertEquals(uuid2.toString().replace("-", ""), array.toHexString());
        // now try to deserialize the byte array into two longs and recreate the same UUID; should be exactly the same
        ByteArray msba = array.subarray(0, 8); // first 8 bytes should be msb
        ByteArray lsba = array.subarray(8, 8); // second 8 bytes should be lsb
        UUID uuid3 = new UUID(msba.toBigInteger().longValue(), lsba.toBigInteger().longValue());
        log.debug("uuid3: {}", uuid2.toString());
        assertEquals(uuid, uuid3);
        assertEquals(uuid3.toString().replace("-", ""), array.toHexString());
    }

    @Test
    public void testBigInteger() {
        BigInteger x = new BigInteger("512").add(new BigInteger("16")).add(new BigInteger("1"));
        // notice there are no leading zeros when serializing the big integer with toString() but if it's negative you'll see the negative sign
        log.debug("X base 2: {}", x.toString(2)); // 1000010001    
        log.debug("X base 16: {}", x.toString(16)); // 211
        // not so when you serialize to byte array... when the big int is 128 bits, it may have add single leading zero which is the sign bit to prevent interpretation of the array as a negative number
    }
}
