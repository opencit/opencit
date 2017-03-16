/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.api;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.My;
import com.intel.mtwilson.saml.TrustAssertion;
import com.intel.mtwilson.saml.TrustAssertion.HostTrustAssertion;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class SamlTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SamlTest.class);

    @Test
    public void testGetSamlByAik() throws Exception {
        ApiClient client = new ApiClient(My.configuration().getConfiguration());
        
        String saml = client.getSamlForHostByAik(new com.intel.mtwilson.model.Sha256Digest("4a805212492a7a5e703f893e091687c3c1b6682d003d4a46b67b5e22c3ff1eed"), true); // throws ApiException, SignatureException ; true means we want to force a fresh attestation; set to false if it's ok to get a cached rseponse
        log.info("Received trust assertion for aik");
        TrustAssertion trustAssertion = client.verifyTrustAssertion(saml);
        log.debug("trust status for {}", trustAssertion.getHosts());
        log.debug("trust assertion valid? {}", trustAssertion.isValid());
        HostTrustAssertion hostTrustAssertion = trustAssertion.getTrustAssertion("4a805212492a7a5e703f893e091687c3c1b6682d003d4a46b67b5e22c3ff1eed");
        log.debug("trust assertion for host {}", hostTrustAssertion);
        log.debug("host is trusted? {}", hostTrustAssertion.isHostTrusted());
        if (hostTrustAssertion.isHostTrusted()) {
            log.info("Host is trusted with aik");
        }
        else {
            log.error("Host is not trusted with aik");
        }
    }
}
