/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.api;

import javax.ws.rs.core.MediaType;

/**
 *
 * @since 0.5.2
 * @author jbuhacoff
 */
public class ApiResponse {
    public int httpStatusCode;
    public String httpReasonPhrase;
    public MediaType contentType; 
    public byte[] content; // if it's a string we assume encoding is UTF-8
    
    /**
     * 
     * @param httpStatusCode
     * @param httpReasonPhrase
     * @param contentType
     * @param content may be null if the server did not return a response BODY (and this may be fine if the status code is 200 OK for a write request)
     */
    public ApiResponse(int httpStatusCode, String httpReasonPhrase, MediaType contentType, byte[] content) {
        this.httpStatusCode = httpStatusCode;
        this.httpReasonPhrase = httpReasonPhrase;
        this.contentType = contentType;
        this.content = content;
    }    
}
