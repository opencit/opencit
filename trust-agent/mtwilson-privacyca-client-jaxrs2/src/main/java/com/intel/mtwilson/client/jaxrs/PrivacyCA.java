/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.privacyca.v2.model.*;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class PrivacyCA extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public PrivacyCA(URL url) {
        super(url);
    }

    public PrivacyCA(Properties properties) throws Exception {
        super(properties);
    }
    public PrivacyCA(Configuration configuration) throws Exception {
        super(configuration);
    }
    
    public X509Certificate endorseTpm(byte[] ekModulus) {
        log.debug("target: {}", getTarget().getUri().toString());
        X509Certificate ec = getTarget()
                .path("/privacyca/tpm-endorsement")
                .request()
                .accept(OtherMediaType.APPLICATION_PKIX_CERT)
                .post(Entity.json(ekModulus), X509Certificate.class);
        return ec;
    }
    
    public byte[] identityChallengeRequest(IdentityChallengeRequest request) {
        log.debug("target: {}", getTarget().getUri().toString());
        byte[] challenge = getTarget()
                .path("/privacyca/identity-challenge-request")
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .post(Entity.json(request), byte[].class);
        return challenge;
    }

    public byte[] identityChallengeResponse(byte[] encrypted) {
        log.debug("target: {}", getTarget().getUri().toString());
        byte[] identity = getTarget()
                .path("/privacyca/identity-challenge-response")
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .post(Entity.entity(encrypted, MediaType.APPLICATION_OCTET_STREAM), byte[].class);
        return identity;
    }


}
