/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.http;

import com.intel.dcsg.cpg.crypto.RsaCredential;
import java.io.IOException;
import java.security.SignatureException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;

/**
 * This class adds an http Authorization header using the "MtWilson" custom scheme.
 * 
 * @since 0.5.2
 * @author jbuhacoff
 */
public class ApacheRsaHttpAuthorization implements ApacheHttpAuthorization {
    private Logger log = LoggerFactory.getLogger(getClass());

    private RsaAuthorization authority;
    
    public ApacheRsaHttpAuthorization(RsaCredential credential) {
        authority = new RsaAuthorization(credential);
    }
    
    @Override
    public void addAuthorization(HttpRequest request) throws SignatureException {
        HashMap<String,String> headers = new HashMap<String,String>();
        request.addHeader("Authorization",
                authority.getAuthorizationQuietly(request.getRequestLine().getMethod(), request.getRequestLine().getUri(), headers));
        // the RsaAuthorization class may generate headers for the request such as nonce and date, so we look for those and add them.
        for(String key : headers.keySet()) {
            request.addHeader(key, headers.get(key));
        }
    }

    /**
     * The entity must be repeatable. If the entity is null then an empty string is used to represent it.
     * @param request 
     */
    @Override
    public void addAuthorization(HttpEntityEnclosingRequest request) throws SignatureException, IOException {
        if( request.getEntity() == null ) {
            addAuthorization((HttpRequest)request);
            return;
        }
        if( !request.getEntity().isRepeatable() ) {
            throw new IllegalArgumentException("Cannot sign a non-repeatable request");
        }
        String body = IOUtils.toString(request.getEntity().getContent());
        
        HashMap<String,String> headers = new HashMap<String,String>();
        request.addHeader("Authorization",
                authority.getAuthorizationQuietly(request.getRequestLine().getMethod(), request.getRequestLine().getUri(), headers, body));
        // the RsaAuthorization class may generate headers for the request such as nonce and date, so we look for those and add them.
        for(String key : headers.keySet()) {
            request.addHeader(key, headers.get(key));
        }
    }
    
}
