/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.wlm.helper;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.security.jersey.AuthenticationJerseyFilter;
import com.intel.mtwilson.security.jersey.HmacRequestVerifier;
import com.intel.mtwilson.security.jersey.X509RequestVerifier;
import com.intel.mtwilson.security.jpa.ApiClientBO;
import com.intel.mtwilson.security.jpa.ApiClientX509BO;
import java.io.IOException;
import javax.ws.rs.container.ContainerRequestFilter;
/**
 * Adapts the AuthenticationJerseyFilter from the MtWilsonHttpSecurity package
 * to this application by configuring it with both X509 and MtWilson authentication
 * schemes. The filter tries X509, then PublicKey, then MtWilson  (Hmac).
 * @since 0.5.1
 * @author jbuhacoff
 */
public class WLMAuthenticationFilter extends AuthenticationJerseyFilter implements ContainerRequestFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WLMAuthenticationFilter.class);
   
    public WLMAuthenticationFilter() {
        try {
            // application-specific configuration
            setRequestValidator(new HmacRequestVerifier(new ApiClientBO(My.persistenceManager().getMSData())));
            setRequestValidator(new X509RequestVerifier(new ApiClientX509BO(My.persistenceManager().getMSData())));
            setTrustedRemoteAddress(MSConfig.getConfiguration().getStringArray("mtwilson.api.trust"));
            setSslRequired(MSConfig.getConfiguration().getBoolean("mtwilson.ssl.required", true));
        } catch (IOException ex) {
            log.error("Error during persistence manager initialization", ex);
            throw new ASException(ErrorCode.SYSTEM_ERROR, ex.getClass().getSimpleName());            
        }
    }

}
