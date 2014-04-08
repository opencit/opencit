/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import org.junit.Test;
import com.intel.mtwilson.shiro.authc.host.*;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class HostMatchTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostMatchTest.class);

    @Test
    public void testFilterLocalhostIPv4() {
        HostAllowCsvFilter filter = new HostAllowCsvFilter("127.0.0.1");
        assertTrue(filter.accept("127.0.0.1"));
        assertFalse(filter.accept("127.0.0.0"));
        assertFalse(filter.accept("127.0.0.2"));
        assertFalse(filter.accept("127.0.0.0/24"));
        assertFalse(filter.accept("192.168.1.100"));
        assertFalse(filter.accept("hostname"));
    }
    @Test
    public void testFilterLocalhost() {
        HostAllowCsvFilter filter = new HostAllowCsvFilter("localhost");
        assertTrue(filter.accept("localhost"));
        assertFalse(filter.accept("locahost"));
        assertFalse(filter.accept("127.0.0.1"));
        assertFalse(filter.accept("localhost.localdomain"));
    }
    @Test
    public void testFilterCsv() {
        HostAllowCsvFilter filter = new HostAllowCsvFilter("127.0.0.1,banana.com,192.168.1.100");
        assertTrue(filter.accept("127.0.0.1"));
        assertTrue(filter.accept("banana.com"));
        assertTrue(filter.accept("192.168.1.100"));
        assertFalse(filter.accept("localhost"));
        assertFalse(filter.accept("carrot.com"));
    }
    
    @Test
    public void testMatch() {
        HostToken token = new HostToken("127.0.0.1");
        HostAllowCsvFilter filter = new HostAllowCsvFilter("127.0.0.1");
        HostAuthenticationInfo info = new HostAuthenticationInfo();
        info.setPrincipals(null);
        info.setCredentials(filter);
        HostCredentialsMatcher matcher = new HostCredentialsMatcher();
        assertTrue(matcher.doCredentialsMatch(token, info));
    }
    
}
