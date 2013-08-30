package com.intel.dcsg.cpg.net;

/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author jbuhacoff
 */
public class InetAddressTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Test
    public void testInetAddress() throws UnknownHostException {
        InetAddress a = InetAddress.getByName("10.1.71.80");
        log.debug(a.toString()); // prints  "/ip"  because a reverse hostname lookup is not possible,  and optional
    }

    @Test
    public void testInvalidInetAddress() throws UnknownHostException {
        InetAddress a = InetAddress.getByName("10.1.x71.80"); // throws UnknownHostException because we don't have DNS... 
        log.debug(a.toString()); 
    }
    
    @Test
    public void testInetAddressLookup() throws UnknownHostException {
        InetAddress b = InetAddress.getByName("mtwilsondev"); // throws UnknownHostException because we don't have DNS...
        log.debug(b.toString()); // would print  "mtwilsondev/10.1.71.80" if it could look it up
    }
    
    @Test
    public void testIPv6Format() {
        IPv6Address a = new IPv6Address("1762:0:0:0:0:B03:1:AF18");
        assertTrue(a.isValid());

        IPv6Address b = new IPv6Address("1762:0:0:0:0:B03:127.32.67.15");
        assertTrue(b.isValid());
    
        IPv6Address c = new IPv6Address("1762:0z:0:0:0:B03:127.32.67.15"); // letter z does not belong
        assertFalse(c.isValid());
    
        IPv6Address d = new IPv6Address("1762:0:0:0:0:0:B03:127.32.67.15"); // extra word of 0
        assertFalse(d.isValid());
    }
    
    @Test
    public void testIPv4Format() {
        IPv4Address a = new IPv4Address("127.0.0.1");
        assertTrue(a.isValid());

        IPv4Address b = new IPv4Address("255.255.255.255");
        assertTrue(b.isValid());
    
        IPv4Address c = new IPv4Address("0.0.0.0");
        assertTrue(c.isValid());

        IPv4Address d = new IPv4Address("192.168.z.57"); // z does not eblong
        assertFalse(d.isValid());
    
        IPv4Address e = new IPv4Address("192.168.0.57 "); // trailing space should be trimmed automatically
        assertTrue(e.isValid());
    
    
    }
    
    @Test
    public void testJsonSerialize() throws IOException {
        InternetAddress address = new InternetAddress("localhost");
        ObjectMapper mapper = new ObjectMapper(); // or from  org.fasterxml.jackson.databind.ObjectMapper
        log.debug("InternetAddress: {}", mapper.writeValueAsString(address));
    }

    @Test
    public void testJsonDeserialize() throws IOException {
        String input = "\"localhost\""; // json, so quotes are part of the input
        ObjectMapper mapper = new ObjectMapper(); // or from  org.fasterxml.jackson.databind.ObjectMapper
        InternetAddress address = mapper.readValue(input, InternetAddress.class);
        log.debug("InternetAddress: {}", mapper.writeValueAsString(address));
    }
    
    
}
