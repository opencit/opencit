/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.extensions.WhiteboardExtensionProvider;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class PropertiesTlsPolicyFactoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertiesTlsPolicyFactoryTest.class);

    @BeforeClass
    public static void registerExtensions() {
        WhiteboardExtensionProvider.register(TlsPolicyCreator.class, CertificateDigestTlsPolicyCreator.class);
    }
    
    @Test
    public void testCertificateDigestPolicy() {
        Properties p = new Properties();
        p.setProperty("mtwilson.api.tls.policy.certificate.sha1", "a5a776c164e1198b12340c452a2b4e8ce709d61f");
        TlsPolicy tlsPolicy = PropertiesTlsPolicyFactory.createTlsPolicy(p);
        log.debug("TlsPolicy class {}", tlsPolicy.getClass().getName());
    }
}
