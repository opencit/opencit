/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;

import com.intel.dcsg.cpg.io.ByteArrayResource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import com.intel.dcsg.cpg.io.UUID;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TestUUID {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testEmptyConstructor() throws IOException {
        UUID uuid = new UUID(); // new random UUID
        log.debug("New UUID: {}", uuid);
        assertEquals(16, uuid.toByteArray().length());
    }
    
    @Test
    public void testUUIDConstructor() {
        java.util.UUID uuid0 = java.util.UUID.randomUUID();
        log.debug("Java UUID: {}", uuid0);
        UUID uuid = UUID.valueOf(uuid0);
        log.debug("New UUID: {}", uuid);
        assertEquals(16, uuid.toByteArray().length());
        assertEquals(uuid0.toString(), uuid.toString());
        log.debug("Hex: {}", uuid.toHexString());
        log.debug("Java: {}", uuid.uuidValue());
        BigInteger big = uuid.toBigInteger();
        UUID uuid2 = UUID.valueOf(big);
        assertEquals(16, uuid2.toByteArray().length());
        assertEquals(uuid.toHexString(), uuid2.toHexString());
        UUID uuid3 = UUID.valueOf(uuid.toByteArray());
        assertEquals(16, uuid3.toByteArray().length());
        assertEquals(uuid.toHexString(), uuid3.toHexString());
        UUID uuid4 = UUID.valueOf(uuid0.toString());
        assertEquals(16, uuid4.toByteArray().length());
        assertEquals(uuid.toHexString(), uuid4.toHexString());
        UUID uuid5 = UUID.valueOf(uuid.toHexString());
        assertEquals(16, uuid5.toByteArray().length());
        assertEquals(uuid.toHexString(), uuid5.toHexString());
   }

    @Test
    public void testUUIDValues() {
        log.debug(UUID.valueOf("a137a43e-03af-486a-ba05-3e0b4c159582").toHexString());
        log.debug(UUID.valueOf("0137a43e-03af-486a-ba05-3e0b4c159582").toHexString());
        log.debug(UUID.valueOf("0037a43e-03af-486a-ba05-3e0b4c159582").toHexString());
        log.debug(UUID.valueOf("0007a43e-03af-486a-ba05-3e0b4c159582").toHexString());
        log.debug(UUID.valueOf("0000a43e-03af-486a-ba05-3e0b4c159582").toHexString());
        log.debug(UUID.valueOf("0000043e-03af-486a-ba05-3e0b4c159582").toHexString());
        log.debug(UUID.valueOf("0000003e-03af-486a-ba05-3e0b4c159582").toHexString());
    }

    @Test
    public void testUUIDStrings() {
        log.debug(UUID.valueOf("a137a43e-03af-486a-ba05-3e0b4c159582").toString());
        log.debug(UUID.valueOf("0137a43e-03af-486a-ba05-3e0b4c159582").toString());
        log.debug(UUID.valueOf("0037a43e-03af-486a-ba05-3e0b4c159582").toString());
        log.debug(UUID.valueOf("0007a43e-03af-486a-ba05-3e0b4c159582").toString());
        log.debug(UUID.valueOf("0000a43e-03af-486a-ba05-3e0b4c159582").toString());
        log.debug(UUID.valueOf("0000043e-03af-486a-ba05-3e0b4c159582").toString());
        log.debug(UUID.valueOf("0000003e-03af-486a-ba05-3e0b4c159582").toString());
        
        log.debug(UUID.valueOf("00426be8-f80f-4145-9c07-1d89afd7438b").toString());
        log.debug(UUID.valueOf("0053c7f3-3a65-45eb-a8b2-be51f9bf57dc").toString());
        log.debug(UUID.valueOf("000141e5-93ca-412d-b934-d5c0f0cdab97").toString());
        log.debug(UUID.valueOf("ffad2346-fa52-4b93-8485-dd18eaa63d28").toString());
        log.debug(UUID.valueOf("ffcbea2e-b26b-470c-839d-4693458342a5").toString());
    }

    /**
     * Example of a "short" UUID only 15 bytes long produced by java.util.UUID.randomUUID()
     * [-62, 56, -93, -42, 56, 67, 111, -68, 53, 95, 50, -28, 112, -108, 127]
     * before the 20 Feb 2014  fix to our UUID constructor around use of java.util.UUID.randomUUID
     * a short UUID caused an ArrayIndexOutOfBoundsException when calling toString().
     * with the fix, short UUIDs are padded with leading zeros immediately after generation to ensure
     * they are exactly 16 bytes, and long UUIDs of 17 bytes are truncated by removing the 
     * least significant bytes until they are exactly 16 bytes
     */
    @Test
    public void testUUIDCreation() {
//        for (int i=0; i<Integer.MAX_VALUE; i++) { // was successful but takes a long time so disabled to prevent stalling a build
        for (int i=0; i<1000; i++) {
            UUID uuid = new UUID();
            try {
                //log.debug(uuid.toString());
                uuid.toString();
            }
            catch(Exception e) {
                log.error("toString failed", e);
                log.debug("bytes {}", uuid.toByteArray().getBytes());
                log.debug("bytearray hex", uuid.toByteArray().toHexString());
                log.debug("uuid {}", uuid.uuidValue().toString());
                break;
            }
        }
    }

    
    @Test
    public void testUUIDCreationWithJavaUtilUuid() {
//        for (int i=0; i<Integer.MAX_VALUE; i++) { // was successful but takes a long time so disabled to prevent stalling a build
        for (int i=0; i<1000; i++) {
            UUID uuid = UUID.valueOf(java.util.UUID.randomUUID());
            try {
//                log.debug(uuid.toString());
                uuid.toString();
            }
            catch(Exception e) {
                log.error("toString failed", e);
                log.debug("bytes {}", uuid.toByteArray().getBytes());
                log.debug("bytearray hex", uuid.toByteArray().toHexString());
                log.debug("uuid {}", uuid.uuidValue().toString());
                break;
            }
        }
    }
    
}
