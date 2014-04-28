/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.http.jaxrs;
//import com.intel.mountwilson.http.security.adapter.*;
import javax.ws.rs.client.ClientRequestFilter;
import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import com.intel.mtwilson.security.http.RsaAuthorization;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.MultivaluedMap;

/**
 * This is a HTTP CLIENT filter to handle OUTGOING requests.
 * 
 * Sample usage:
 * 
        clientConfig = new ClientConfig();
        RsaCredentialX509 credential = new RsaCredentialX509(privateKey, x509Certificate);
        clientConfig.register(new X509AuthorizationFilter(credential));
 * 
 * Example output header:
 * Authorization: X509 fingerprint="7Rt5AyFkoqGekveyBb53nG3EVaiPy2ZRdTgQ59tcI00=", headers="X-Nonce,Date", algorithm="SHA256withRSA", signature="PdEREUSh6y/Y8H+QMBTg3hJVkS7eb+/9f7/RdaWRbAq+yycNBhjq2iYn3wFs7pZjJtlwK/KbpzU7ZKoyKHx70f/ivqhjyJhUMWCWFD/qZcQToevaosDwAXJH0uXiJQZPP16n0D7ZFqJ435vj9MujR3kVDb+lFGb+YyRUIOAmIKf2AaCXHATDi3cMYpACN/FxFvszoAmNvWmocR41aLdwD/RAMOmZH60qlT3vWW1/76BYyRaG8L/5VIImP79fWOdsmujN6hktabohXdo2Pr9udtHTRreaUslFI5/hnowmQvUQAEUePCPF2QnkOc6iwrwKSzcdlwoqaumu2XK9EqxzWg=="
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
public class X509AuthorizationFilter implements ClientRequestFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509AuthorizationFilter.class);

    private RsaAuthorization auth;
    
    public X509AuthorizationFilter(RsaCredentialX509 credential) {
        auth = new RsaAuthorization(credential);
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
            // first convert the headers we want to sign into to a map<string,string>   
            MultivaluedMap<String,Object> headers = requestContext.getHeaders();
            HashMap<String,String> map = new HashMap<>();
            if( headers.containsKey("Date")) {
                map.put("Date", headers.getFirst("Date").toString());
            }
            log.debug("Request context URL: {}", requestContext.getUri().toURL().toString());
            String header;
            if( requestContext.getEntity() == null ) {
                header = auth.getAuthorization(requestContext.getMethod(), requestContext.getUri().toURL().toString(), map);
            }
            else {
                header = auth.getAuthorization(requestContext.getMethod(), requestContext.getUri().toURL().toString(), map, requestContext.getEntity().toString());            
            }
            
            // the authorization class adds Date header if we don't already have it, and it adds an X-Nonce header which we need to include in our request
            for(String headerName : map.keySet()) {
                headers.putSingle(headerName, map.get(headerName));
            }
            
            log.debug("Authorization: {}", header);
            requestContext.getHeaders().add("Authorization", header);
            
        }
        catch(NoSuchAlgorithmException | InvalidKeyException | IOException | SignatureException e) {
            throw new IOException(e);
        }
        
    }
    
}
