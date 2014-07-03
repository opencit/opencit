/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.helper;

import com.intel.mtwilson.My;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.security.jersey.AuthenticationJerseyFilter;
import com.intel.mtwilson.security.jersey.HmacRequestVerifier;
import com.intel.mtwilson.security.jersey.HttpBasicRequestVerifier;
import com.intel.mtwilson.security.jersey.X509RequestVerifier;
import com.intel.mtwilson.security.jpa.ApiClientBO;
import com.intel.mtwilson.security.jpa.ApiClientHttpBasicBO;
import com.intel.mtwilson.security.jpa.ApiClientX509BO;
import com.intel.mtwilson.security.jpa.RequestLogBO;
import java.io.IOException;
import javax.ws.rs.container.ContainerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Adapts the AuthenticationJerseyFilter from the MtWilsonHttpSecurity package
 * to this application by configuring it with both X509 and MtWilson authentication
 * schemes. The filter tries X509, then PublicKey, then MtWilson  (Hmac).
 * @since 0.5.1
 * @author jbuhacoff
 */
public class ASAuthenticationFilter extends AuthenticationJerseyFilter implements ContainerRequestFilter {
    private static Logger log = LoggerFactory.getLogger(ASAuthenticationFilter.class);
    
    public ASAuthenticationFilter() throws IOException {
        // application-specific configuration
        setRequestLog(new RequestLogBO(My.persistenceManager().getASData()));
        setRequestValidator(new HmacRequestVerifier(new ApiClientBO(My.persistenceManager().getMSData())));
        setRequestValidator(new X509RequestVerifier(new ApiClientX509BO(My.persistenceManager().getMSData())));
        // Since we might want to support HttpBasic in OpenSource, we have added the corresponding JPA controller in the AttestationService
        setRequestValidator(new HttpBasicRequestVerifier(new ApiClientHttpBasicBO(My.persistenceManager().getASData())));
        setTrustedRemoteAddress(MSConfig.getConfiguration().getStringArray("mtwilson.api.trust"));        
        setSslRequired(MSConfig.getConfiguration().getBoolean("mtwilson.ssl.required", true));
    }

}
