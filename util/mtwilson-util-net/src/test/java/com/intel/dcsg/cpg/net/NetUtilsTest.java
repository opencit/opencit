/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.net;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class NetUtilsTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NetUtilsTest.class);

    @Test
    public void testGetLocalAddresses() throws SocketException {
        List<String> addresses = NetUtils.getNetworkAddressList();
        log.debug("addresses: {}", addresses);
    }
    @Test
    public void testGetLocalHostnames() throws SocketException {
        List<String> hostnames = NetUtils.getNetworkHostnameList();
        log.debug("hostnames: {}", hostnames);
    }

}
