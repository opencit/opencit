/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcg.io;

import com.intel.dcsg.cpg.io.AllCapsEnvironmentConfiguration;
import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author jbuhacoff
 */
public class TestAllCapsEnvironmentConfiguration {
    @Test
    public void testAllCapsTranformation() {
        AllCapsEnvironmentConfiguration conf = new AllCapsEnvironmentConfiguration();
        assertEquals("MTWILSON_API_SSL_REQUIRE_TRUSTED_CERTIFICATE", conf.toAllCaps("mtwilson.api.ssl.requireTrustedCertificate"));
        assertEquals("MTWILSON_API_SSL_REQUIRE_TRUSTED_CERTIFICATE", conf.toAllCaps("MTWILSON_API_SSL_REQUIRE_TRUSTED_CERTIFICATE"));
        assertEquals("MTWILSON_API_SSL_VERIFY_HOSTNAME", conf.toAllCaps("mtwilson.api.ssl.verifyHostname"));
        assertEquals("MTWILSON_API_SSL_VERIFY_HOSTNAME", conf.toAllCaps("mtwilson.api.SSL_verifyHostname"));
    }
}
