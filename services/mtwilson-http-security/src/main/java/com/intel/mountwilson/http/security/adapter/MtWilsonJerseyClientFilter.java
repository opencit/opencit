/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.http.security.adapter;

import java.io.IOException;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientRequestContext;
//import javax.ws.rs.client.ClientResponseFilter;
//import javax.ws.rs.container.ContainerRequestContext;

//import com.sun.jersey.api.client.ClientHandlerException;
//import com.sun.jersey.api.client.ClientRequest;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.filter.ClientFilter;
import com.intel.mtwilson.security.http.HmacAuthorization;
import com.intel.dcsg.cpg.crypto.HmacCredential;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * This is a HTTP CLIENT filter to handle OUTGOING requests.
 * 
 * Depends on the jersey-client package which provides the interface
 * com.sun.jersey.api.client.filter.ClientFilter
 * 
 * Sample usage:
 * 
        import com.sun.jersey.api.client.Client;
        ...
        MtWilsonJerseyClientFilter filter = new MtWilsonJerseyClientFilter("client id","secret key");
        Client client = Client.create();
        client.addFilter(filter);
        String result = client.resource("http://localhost:8080/WLMService/resources").path("os").accept(MediaType.TEXT_PLAIN).get(String.class);
        System.out.println(result);
 * 
 * Because this filter creates an Authorization header with a signature over the http method, URL, and entity body (if provided), 
 * it should be the LAST filter that is applied so that it can sign the final form of the entity body. The only exception to that
 * would be if a server filter decodes the entity body BEFORE the security filter, for example gzip compression. In any such case,
 * you must take care to match the order in which the filters are applied on the client and server.
 * 
 * @author jbuhacoff
 * @since 0.5.1
 */
//public class MtWilsonJerseyClientFilter extends ClientFilter {
public class MtWilsonJerseyClientFilter implements ClientRequestFilter {
//    private static Log log = LogFactory.getLog(MtWilsonJerseyClientFilter.class);
    private HmacAuthorization auth;
    //ContainerRequestContext crc = new ClientRequestFilter() {

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        try {
            String header;
            if( requestContext.getEntity() == null ) {
                header = auth.getAuthorization(requestContext.getMethod(), requestContext.getUri().toURL().toString());
            }
            else {
                header = auth.getAuthorization(requestContext.getMethod(), requestContext.getUri().toURL().toString(), requestContext.getEntity().toString());            
            }
            requestContext.getHeaders().add("Authorization", header);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
        
    public MtWilsonJerseyClientFilter(String clientId, String secretKey) {
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
//    @Override
//    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
//        // Modify the request
//        try {
//            String header;
//            if( cr.getEntity() == null ) {
//                header = auth.getAuthorization(cr.getMethod(), cr.getURI().toURL().toString());
//            }
//            else {
//                header = auth.getAuthorization(cr.getMethod(), cr.getURI().toURL().toString(), cr.getEntity().toString());            
//            }
//            cr.getHeaders().add("Authorization", header);
//            
//        }
//        catch(Exception e) {
//            throw new ClientHandlerException(e);
//        }
//        
//        // Call the next client handler in the filter chain
//        ClientResponse resp = getNext().handle(cr);
//        
//        // Return the response; we don't need to modify it in the current implementation
//        // but if the server implements server-nonces or tokens then here we would look for
//        // a 403 forbidden message with the token and automatically re-send the request once
//        // with authorization using the new token before returning the final response
//        return resp;
//    }
    
}
