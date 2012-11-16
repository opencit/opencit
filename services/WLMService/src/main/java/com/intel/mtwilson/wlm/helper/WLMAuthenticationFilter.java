/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.wlm.helper;

import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.security.jersey.AuthenticationJerseyFilter;
import com.intel.mtwilson.security.jersey.HmacRequestVerifier;
import com.intel.mtwilson.security.jersey.X509RequestVerifier;
import com.intel.mtwilson.security.jpa.ApiClientBO;
import com.intel.mtwilson.security.jpa.ApiClientX509BO;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Adapts the AuthenticationJerseyFilter from the MtWilsonHttpSecurity package
 * to this application by configuring it with both X509 and MtWilson authentication
 * schemes. The filter tries X509, then PublicKey, then MtWilson  (Hmac).
 * @since 0.5.1
 * @author jbuhacoff
 */
public class WLMAuthenticationFilter extends AuthenticationJerseyFilter implements ContainerRequestFilter {
    private static Logger log = LoggerFactory.getLogger(WLMAuthenticationFilter.class);
    private WLMPersistenceManager persistenceManager = new WLMPersistenceManager();
   
    public WLMAuthenticationFilter() {
        // application-specific configuration
        setRequestValidator(new HmacRequestVerifier(new ApiClientBO(persistenceManager.getEntityManagerFactory("MSDataPU"))));
        setRequestValidator(new X509RequestVerifier(new ApiClientX509BO(persistenceManager.getEntityManagerFactory("MSDataPU"))));
        setTrustedRemoteAddress(MSConfig.getConfiguration().getStringArray("mtwilson.api.trust"));
        setSslRequired(MSConfig.getConfiguration().getBoolean("mtwilson.ssl.required", true));
    }

}
