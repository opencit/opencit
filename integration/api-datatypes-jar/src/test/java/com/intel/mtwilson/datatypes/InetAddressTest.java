/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class InetAddressTest {
    @Test
    public void testInetAddress() throws UnknownHostException {
        InetAddress a = InetAddress.getByName("10.1.71.80");
        System.out.println(a.toString()); // prints  "/ip"  because a reverse hostname lookup is not possible,  and optional
    }

    @Test
    public void testInvalidInetAddress() throws UnknownHostException {
        InetAddress a = InetAddress.getByName("10.1.x71.80"); // throws UnknownHostException because we don't have DNS... 
        System.out.println(a.toString()); 
    }
    
    @Test
    public void testInetAddressLookup() throws UnknownHostException {
        InetAddress b = InetAddress.getByName("mtwilsondev"); // throws UnknownHostException because we don't have DNS...
        System.out.println(b.toString()); // would print  "mtwilsondev/10.1.71.80" if it could look it up
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
}
