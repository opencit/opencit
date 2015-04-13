/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.http.jaxrs;
import javax.ws.rs.client.ClientRequestFilter;
import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import java.io.IOException;
import javax.ws.rs.Priorities;

/**
 * This is a HTTP CLIENT filter to handle OUTGOING requests.
 * 
 * Sample usage:
 * 
        clientConfig = new ClientConfig();
        clientConfig.register(new TokenAuthorizationFilter("token-value"));
 * 
 * Example http header added:
 * <pre>
 * Authorization: Token AAABRClYrajNQAz3bBcQ3oC9O/3J02Ok
 * </pre>
 * 
 * @author jbuhacoff
 * @since 3.0
 */
@Priority(Priorities.AUTHORIZATION)
public class TokenAuthorizationFilter implements ClientRequestFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenAuthorizationFilter.class);

    private String tokenValue;
    
    public TokenAuthorizationFilter(String tokenValue) {
        this.tokenValue = tokenValue;
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
            String header = String.format("Token %s", tokenValue);
            log.debug("Authorization: {}", header);
            requestContext.getHeaders().add("Authorization", header);
            
        }
        catch(Exception e) {
            throw new IOException(e);
        }
        
    }
    
}
