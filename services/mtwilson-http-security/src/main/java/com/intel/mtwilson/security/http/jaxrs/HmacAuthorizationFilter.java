/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.http.jaxrs;
import com.intel.mountwilson.http.security.adapter.*;
import javax.ws.rs.client.ClientRequestFilter;
import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import com.intel.mtwilson.security.http.HmacAuthorization;
import com.intel.dcsg.cpg.crypto.HmacCredential;
import java.io.IOException;
import javax.ws.rs.Priorities;

/**
 * This is a HTTP CLIENT filter to handle OUTGOING requests.
 * 
 * Sample usage:
 * 
        clientConfig = new ClientConfig();
        clientConfig.register(new HmacAuthorizationFilter("username", "password"));
 * 
 * Example http header added:
 * Authorization: MtWilson http_method="GET", uri="http://localhost:8080/v2/files", username="dXNlcm5hbWU=", nonce="AAABRClYrajNQAz3bBcQ3oC9O/3J02Ok", signature_method="HmacSHA256", timestamp="2014-02-12T19:44:41-0800", signature="QZhSxxsH28mHR7Crp4FZg1lwYK7ya4wTCZIZ4+y8vK8="
 * 
 * Because this filter creates an Authorization header with a signature over the http method, URL, and entity body (if provided), 
 * it should be the LAST filter that is applied so that it can sign the final form of the entity body. The only exception to that
 * would be if a server filter decodes the entity body BEFORE the security filter, for example gzip compression. In any such case,
 * you must take care to match the order in which the filters are applied on the client and server.
 * 
 * @author jbuhacoff
 * @since 2.0
 */
@Priority(Priorities.AUTHORIZATION)
public class HmacAuthorizationFilter implements ClientRequestFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HmacAuthorizationFilter.class);

    private HmacAuthorization auth;
    
    public HmacAuthorizationFilter(String clientId, String secretKey) {
        auth = new HmacAuthorization(new HmacCredential(clientId, secretKey));
    }
    
    /**
     * This method assumes that the entity body of the request is either null or a String or
     * has a toString() method that returns the String that should be signed.
     * 
     * @param cr
     * @return
     * @throws ClientHandlerException 
     */
    @Override
    public void filter(ClientRequestContext requestContext)
                        throws IOException { 
        // Modify the request
        try {
            String header;
            if( requestContext.getEntity() == null ) {
                header = auth.getAuthorization(requestContext.getMethod(), requestContext.getUri().toURL().toString());
            }
            else {
                header = auth.getAuthorization(requestContext.getMethod(), requestContext.getUri().toURL().toString(), requestContext.getEntity().toString());            
            }
            log.debug("Authorization: {}", header);
            requestContext.getHeaders().add("Authorization", header);
            
        }
        catch(Exception e) {
            throw new IOException(e);
        }
        
    }
    
}
