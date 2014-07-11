/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.server.filter;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * @author jbuhacoff
 */
public class ErrorLogFilter implements ContainerResponseFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ErrorLogFilter.class);

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        Response.Status.Family httpStatusFamily = Response.Status.Family.familyOf(response.getStatus());
        if( httpStatusFamily == Response.Status.Family.CLIENT_ERROR || httpStatusFamily == Response.Status.Family.SERVER_ERROR ) {
            String incidentTag = RandomUtil.randomHexString(4); // 4 bytes => 8 hex digits  
            log.debug("Incident Tag #{}", incidentTag);
//            response.getStringHeaders().add("Incident-Tag", incidentTag); // causes exception: java.lang.UnsupportedOperationException  thrown by the string headers map 
            response.getHeaders().add("Incident-Tag", incidentTag);
        }
    }
    
}
