/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.CaCertificates;
import com.intel.mtwilson.My;
import java.security.cert.X509Certificate;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class CaCertificatesTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CaCertificatesTest.class);

    private static CaCertificates client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new CaCertificates(My.configuration().getClientProperties());
    }
    
    @Test
    public void testRetrieve() throws Exception {
        X509Certificate rootCertificate = client.retrieveCaCertificate("root");
        X509Certificate tlsCertificate = client.retrieveCaCertificate("tls");
        X509Certificate samlCertificate = client.retrieveCaCertificate("saml");
        X509Certificate privacyCertificate = client.retrieveCaCertificate("privacy");
    }

}
