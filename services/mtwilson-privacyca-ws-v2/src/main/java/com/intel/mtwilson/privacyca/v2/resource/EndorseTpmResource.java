/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.resource;

import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.privacyca.v2.model.EndorseTpmRequest;
import com.intel.mtwilson.privacyca.v2.rpc.EndorseTpm;
import java.security.cert.X509Certificate;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
@V2
@RequiresPermissions("tpms:endorse")
@Path("/privacyca/tpm-endorsement")
public class EndorseTpmResource {
    
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces({CryptoMediaType.APPLICATION_PKIX_CERT, MediaType.APPLICATION_OCTET_STREAM, CryptoMediaType.APPLICATION_X_PEM_FILE, MediaType.TEXT_PLAIN})
    public X509Certificate endorseTpm(byte[] ekModulus) throws Exception {
        EndorseTpm rpc = new EndorseTpm();
        rpc.setEkModulus(ekModulus);
        return rpc.call();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({CryptoMediaType.APPLICATION_PKIX_CERT, MediaType.APPLICATION_OCTET_STREAM, CryptoMediaType.APPLICATION_X_PEM_FILE, MediaType.TEXT_PLAIN})
    public X509Certificate endorseTpm(EndorseTpmRequest endorseTpmRequest) throws Exception {
        EndorseTpm rpc = new EndorseTpm();
        rpc.setEkModulus(endorseTpmRequest.getEkModulus());
        return rpc.call();
    }

}
