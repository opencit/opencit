package com.intel.mtwilson.saml;


import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.saml.TrustAssertion.HostTrustAssertion;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */

/**
 *
 * @author jbuhacoff
 */
public class MultihostSamlTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MultihostSamlTest.class);

    private String getSaml() throws Exception {
        InputStream in = getClass().getResourceAsStream("/saml.xml");
        String xml = IOUtils.toString(in);
        in.close();
        return xml;
    }
    private X509Certificate getSamlCertificate() throws Exception {
        InputStream in = getClass().getResourceAsStream("/saml.crt.pem");
        String pem = IOUtils.toString(in);
        in.close();
        return X509Util.decodePemCertificate(pem);
    }
    
    @Test
    public void testVerifyMultihostSaml() throws Exception {
        String saml = getSaml();
        X509Certificate cert = getSamlCertificate();
        TrustAssertion trustAssertion = new TrustAssertion(new X509Certificate[] { cert }, saml);
        Set<String> hostnames = trustAssertion.getHosts();
        for(String hostname : hostnames) {
            log.debug("hostname: {}", hostname);
            HostTrustAssertion hostTrustAssertion = trustAssertion.getTrustAssertion(hostname);
            log.debug("subject: {}", hostTrustAssertion.getSubject());
            log.debug("trusted? {}", hostTrustAssertion.isHostTrusted());
            log.debug("bios trusted? {}", hostTrustAssertion.isHostBiosTrusted());
            log.debug("vmm trusted? {}", hostTrustAssertion.isHostVmmTrusted());
            log.debug("location trusted? {}", hostTrustAssertion.isHostLocationTrusted());
            Set<String> attributeNames = hostTrustAssertion.getAttributeNames();
            for(String attributeName : attributeNames) {
                log.debug("attribute {} = {}", attributeName, hostTrustAssertion.getStringAttribute(attributeName));
            }
        }
    }
}
