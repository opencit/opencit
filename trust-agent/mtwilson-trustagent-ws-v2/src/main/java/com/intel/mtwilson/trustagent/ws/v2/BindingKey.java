/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.ws.v2;

import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.TrustagentRepository;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@V2
@Path("/binding-key-certificate")
public class BindingKey {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BindingKey.class);
    private static X509Certificate binding = null;
    
    protected TrustagentConfiguration getConfiguration() throws IOException {
        return TrustagentConfiguration.loadConfiguration();
    }
    
    @GET
    @Produces({CryptoMediaType.APPLICATION_PKIX_CERT, CryptoMediaType.APPLICATION_X_PEM_FILE})
    public X509Certificate getCertificate() throws IOException, CertificateException {
        if( binding == null ) {
            TrustagentConfiguration configuration = getConfiguration();
            if( configuration.isDaaEnabled() ) {
                log.debug("daa is currently not supported");
                return null;
            }
            else {
                TrustagentRepository repository = new TrustagentRepository(configuration);
                X509Certificate bkCertificate = repository.getBindingKeyCertificate();
                if( bkCertificate == null ) {
                    throw new WebApplicationException(Response.serverError().header("Error", "Cannot load Binding key certificate file").build());
                }
                binding = bkCertificate;
            }
        }
        return binding;
    }
 
}
