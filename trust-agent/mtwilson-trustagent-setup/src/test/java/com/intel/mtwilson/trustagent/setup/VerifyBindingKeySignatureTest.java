/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.x509.X509Util;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class VerifyBindingKeySignatureTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VerifyBindingKeySignatureTest.class);

    @Test
    public void testVerifyBindingKey() throws Exception {
        String pca = IOUtils.toString(getClass().getResourceAsStream("/pca.pem"));
        String bindingkey = IOUtils.toString(getClass().getResourceAsStream("/bindingkey.pem"));
        
        X509Certificate pcacert = X509Util.decodePemCertificate(pca);
        X509Certificate bindingkeycert = X509Util.decodePemCertificate(bindingkey);
        
        bindingkeycert.verify(pcacert.getPublicKey());
    }
}
