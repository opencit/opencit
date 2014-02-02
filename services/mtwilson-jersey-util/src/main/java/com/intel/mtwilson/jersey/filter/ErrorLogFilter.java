/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey.filter;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * TODO make a configurable option that for some status codes (that could
 * be a configurable list) we could store the entire request headers & content 
 * in the database w/ the same id # so engineering team can look at it later
 * 
 * @author jbuhacoff
 */
public class ErrorLogFilter implements ContainerResponseFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ErrorLogFilter.class);
    
    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        if( response.getStatus() != Status.OK.getStatusCode() ) {
            String incidentTag = RandomUtil.randomHexString(4); // 4 bytes => 8 hex digits  
            log.debug("Incident Tag #{}", incidentTag);
//            response.getStringHeaders().add("Incident-Tag", incidentTag); // causes exception: java.lang.UnsupportedOperationException  thrown by the string headers map 
            response.getHeaders().add("Incident-Tag", incidentTag);
        }
    }
    
}
