/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.http.apache;

import com.intel.dcsg.cpg.crypto.HmacCredential;
import com.intel.mtwilson.security.http.HmacAuthorization;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;

/**
 * This class adds an http Authorization header using the "MtWilson" custom scheme.
 * 
 * @since 0.5.2
 * @author jbuhacoff
 */
public class ApacheHmacHttpAuthorization implements ApacheHttpAuthorization {

    private HmacAuthorization authority;
    
    public ApacheHmacHttpAuthorization(HmacCredential credentials) {
        authority = new HmacAuthorization(credentials);
    }
    
    @Override
    public void addAuthorization(HttpRequest request) {
        request.addHeader("Authorization", authority.getAuthorizationQuietly(request.getRequestLine().getMethod(), request.getRequestLine().getUri()));
    }

    /**
     * The entity must be repeatable. If the entity is null then an empty string is used to represent it.
     * @param request 
     */
    @Override
    public void addAuthorization(HttpEntityEnclosingRequest request) throws IOException {
        if( request.getEntity() == null ) {
            request.addHeader("Authorization", authority.getAuthorizationQuietly(request.getRequestLine().getMethod(), request.getRequestLine().getUri(), ""));
            return;
        }
        if( !request.getEntity().isRepeatable() ) {
            throw new IllegalArgumentException("Cannot sign a non-repeatable request");
        }
        String body = IOUtils.toString(request.getEntity().getContent());
        request.addHeader("Authorization", authority.getAuthorizationQuietly(request.getRequestLine().getMethod(), request.getRequestLine().getUri(), body));
    }
    
}
