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
        UUID uuid = new UUID(uuid0);
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
}
