/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateDigestTlsPolicy;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class PolicyCreatorTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PolicyCreatorTest.class);
    
    @Test
    public void testCreateCertificateDigestTlsPolicy() {
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType("certificate-digest");
        tlsPolicyDescriptor.setData(new ArrayList<String>());
        tlsPolicyDescriptor.getData().add("18 9a e6 e0 26 6f ae 63 8f 8c 9c b0 92 e1 ad 04 c3 a7 58 ab");
        CertificateDigestTlsPolicyCreator creator = new CertificateDigestTlsPolicyCreator();
        CertificateDigestTlsPolicy tlsPolicy = creator.createTlsPolicy(tlsPolicyDescriptor);
        assertNotNull(tlsPolicy);
    }
    
}
