/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.resource;

import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.privacyca.v2.model.IdentityChallenge;
import com.intel.mtwilson.privacyca.v2.model.IdentityChallengeRequest;
import com.intel.mtwilson.privacyca.v2.rpc.IdentityRequestGetChallenge;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/privacyca/identity-challenge-request")
public class IdentityRequestGetChallengeResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdentityRequestGetChallengeResource.class);

    /**
     * 
     * @param derEncodedIdentityChallengeRequest is an ASN.1 structure  SEQUENCE ( DEROCTETSTREAM identityRequest , DEROCTETSTREAM endorsementCertificate )
     * @return encrypted identity challenge
     * @throws Exception 
     */
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] identityChallengeRequest(byte[] derEncodedIdentityChallengeRequest) throws Exception {
        IdentityChallengeRequest identityChallengeRequest = IdentityChallengeRequest.valueOf(derEncodedIdentityChallengeRequest);
        IdentityRequestGetChallenge rpc = new IdentityRequestGetChallenge();
        rpc.setTpmVersion(identityChallengeRequest.getTpmVersion());
        rpc.setIdentityRequest(identityChallengeRequest.getIdentityRequest());
        rpc.setAikName(identityChallengeRequest.getAikName());
        rpc.setEndorsementCertificate(identityChallengeRequest.getEndorsementCertificate());
        return rpc.call().getIdentityChallenge();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public IdentityChallenge identityChallengeRequest(IdentityChallengeRequest identityChallengeRequest) throws Exception {
        IdentityRequestGetChallenge rpc = new IdentityRequestGetChallenge();
        rpc.setTpmVersion(identityChallengeRequest.getTpmVersion());
        rpc.setIdentityRequest(identityChallengeRequest.getIdentityRequest());
        rpc.setAikName(identityChallengeRequest.getAikName());
        rpc.setEndorsementCertificate(identityChallengeRequest.getEndorsementCertificate());
        return rpc.call();
    }

}
