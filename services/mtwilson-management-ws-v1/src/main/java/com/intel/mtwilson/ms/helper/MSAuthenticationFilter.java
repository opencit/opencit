/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.helper;

import com.intel.mtwilson.ms.MSPersistenceManager;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.security.jersey.AuthenticationJerseyFilter;
import com.intel.mtwilson.security.jersey.HmacRequestVerifier;
import com.intel.mtwilson.security.jersey.X509RequestVerifier;
import com.intel.mtwilson.security.jpa.ApiClientBO;
import com.intel.mtwilson.security.jpa.ApiClientX509BO;
//import com.sun.jersey.spi.container.ContainerRequestFilter;
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
public class MSAuthenticationFilter extends AuthenticationJerseyFilter implements ContainerRequestFilter {
    private static Logger log = LoggerFactory.getLogger(MSAuthenticationFilter.class);
    private MSPersistenceManager persistenceManager = new MSPersistenceManager();
    public MSAuthenticationFilter() {
        // application-specific configuration
        setRequestValidator(new HmacRequestVerifier(new ApiClientBO(persistenceManager.getEntityManagerFactory("MSDataPU"))));
        setRequestValidator(new X509RequestVerifier(new ApiClientX509BO(persistenceManager.getEntityManagerFactory("MSDataPU"))));
        setTrustedRemoteAddress(MSConfig.getConfiguration().getStringArray("mtwilson.api.trust"));
        setSslRequired(MSConfig.getConfiguration().getBoolean("mtwilson.ssl.required", true));
    }

}
