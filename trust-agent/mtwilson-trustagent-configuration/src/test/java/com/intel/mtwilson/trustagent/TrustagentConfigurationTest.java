/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent;

import java.net.SocketException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class TrustagentConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustagentConfigurationTest.class);

    @Test
    public void testGetTrustagentTlsCertIpArray() throws SocketException {
        PropertiesConfiguration p = new PropertiesConfiguration();
        TrustagentConfiguration c = new TrustagentConfiguration(p);
        long start = System.currentTimeMillis();
        log.debug("ips={}",(Object[])c.getTrustagentTlsCertIpArray());
        long end = System.currentTimeMillis();
        log.debug("time={}ms",(end-start));
    }
    @Test
    public void testGetTrustagentTlsCertDnsArray() throws SocketException {
        PropertiesConfiguration p = new PropertiesConfiguration();
        TrustagentConfiguration c = new TrustagentConfiguration(p);
        long start = System.currentTimeMillis();
        log.debug("ips={}",(Object[])c.getTrustagentTlsCertDnsArray());
        long end = System.currentTimeMillis();
        log.debug("time={}ms",(end-start));
    }
}
