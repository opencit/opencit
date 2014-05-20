/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.privacyca.v2.model.*;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class PrivacyCA extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public PrivacyCA(URL url)throws Exception {
        super(url);
    }

    public PrivacyCA(Properties properties) throws Exception {
        super(properties);
    }
    public PrivacyCA(Configuration configuration) throws Exception {
        super(configuration);
    }
    public PrivacyCA(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }
    
    public X509Certificate endorseTpm(byte[] ekModulus) {
        log.debug("target: {}", getTarget().getUri().toString());
        X509Certificate ec = getTarget()
                .path("/privacyca/tpm-endorsement")
                .request()
                .accept(CryptoMediaType.APPLICATION_PKIX_CERT)
                .post(Entity.entity(ekModulus, MediaType.APPLICATION_OCTET_STREAM), X509Certificate.class);
        return ec;
    }
    
    public IdentityChallenge identityChallengeRequest(IdentityChallengeRequest challengeRequest) {
        log.debug("target: {}", getTarget().getUri().toString());
        IdentityChallenge challenge = getTarget()
                .path("/privacyca/identity-challenge-request")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(challengeRequest), IdentityChallenge.class);
        return challenge;
    }

    public IdentityBlob identityChallengeResponse(IdentityChallengeResponse challengeResponse) {
        log.debug("target: {}", getTarget().getUri().toString());
        IdentityBlob identity = getTarget()
                .path("/privacyca/identity-challenge-response")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(challengeResponse), IdentityBlob.class);
        return identity;
    }


}
