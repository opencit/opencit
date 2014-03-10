/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.resource;

import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.privacyca.v2.rpc.IdentityRequestSubmitResponse;
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
@Path("/privacyca/identity-challenge-response")
public class IdentityRequestSubmitChallengeResponseResource {
    @POST
    @Consumes({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})
    public byte[] identityChallengeResponse(byte[] challengeResponse) throws Exception {
        IdentityRequestSubmitResponse rpc = new  IdentityRequestSubmitResponse();
        rpc.setChallengeResponse(challengeResponse);
        return rpc.call();
    }
}
