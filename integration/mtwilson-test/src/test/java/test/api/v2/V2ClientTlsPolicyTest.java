/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.api.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.attestation.client.jaxrs.Oems;
import com.intel.mtwilson.as.rest.v2.model.Oem;
import com.intel.mtwilson.as.rest.v2.model.OemCollection;
import com.intel.mtwilson.as.rest.v2.model.OemFilterCriteria;
import com.intel.mtwilson.test.RemoteIntegrationTest;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class V2ClientTlsPolicyTest extends RemoteIntegrationTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(V2ClientTlsPolicyTest.class);

    @Test
    public void testSearchOemsWithPublicKeyDigestTlsPolicy() throws Exception {
        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.PublicKeyDigestTlsPolicyCreator.class);
//        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.PublicKeyDigestTlsPolicyCreator.class);
        testProperties.setProperty("mtwilson.api.tls.policy.publickey.sha1", "8e c3 ea 36 0d d8 27 ab 9c 71 cf 11 a6 b7 30 35 a3 dc 23 fd");
        Oems client = new Oems(testProperties);
        OemCollection results = client.searchOems(new OemFilterCriteria());
        List<Oem> list = results.getOems();
        for (Oem oem : list) {
            log.debug("got oem {}", oem.getId().toString());
        }
    }

    @Test(expected=javax.net.ssl.SSLHandshakeException.class)
    public void testSearchOemsWithPublicKeyDigestTlsPolicyWithIncorrectDigest() throws Exception {
        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.PublicKeyDigestTlsPolicyCreator.class);
//        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.PublicKeyDigestTlsPolicyCreator.class);
        testProperties.setProperty("mtwilson.api.tls.policy.publickey.sha1", "8e c3 ea 36 0d d8 27 ab 9c 71 cf 11 a6 b7 30 35 a3 dc 23 f0"); // intentionally incorrect digest to cause an error
        Oems client = new Oems(testProperties);
        OemCollection results = client.searchOems(new OemFilterCriteria());
        List<Oem> list = results.getOems();
        for (Oem oem : list) {
            log.debug("got oem {}", oem.getId().toString());
        }
    }
}
