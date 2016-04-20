/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.http;

import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;

/**
 * This class requires the following libraries:
 * org.apache.commons.lang.StringUtils from commons-lang
 * 
 * This class defines the format of the content which is hashed to produce
 * the HMAC-SHA256 symmetric-key signature for the "MtWilson" authentication
 * scheme. This class is not tied to the signature algorithm and could also
 * be used with other signature algorithms. 
 * 
 * This class is used by RequestAuthorization and VerifyAuthorization for the
 * MtWilson authentication scheme.
 * 
 * See also HttpSignatureInput for a class that is a little more general
 * and allows the inclusion of arbitrary headers (including nonce and timestamp)
 * 
 * @since 0.5.1 
 * @author jbuhacoff
 */
public class HmacSignatureInput {
  
    public String httpMethod;
    public String absoluteUrl;
    public String fromToken;
    public String nonce;
    public String body;
    public String signatureMethod;
    public String timestamp;
    
    /**
     * Generates the request document to be signed. This document represents the
     * request because it includes the Http method, request URL, user-id token,
     * timestamp, signature method, and nonce.
     * 
     * The parameters must not be null and must not contain any newlines (except for the body parameter, which can be empty string or any non-empty string)
     * 
     * Sample output:
     * 
Request: POST http://example.com/some/path
From: username
Timestamp: 2012-02-14T08:15:00-08:00
Nonce: FaaKLOOuyG7/kLVD5vQ7iw==
Signature-Method: HMAC-SHA256

{"numbers":[1,2,3],"notes":"whatever the payload of the request, whether it's plain text, xml, json, or binary file in text encoding"}
     * 
     * @param httpMethod for the HTTP request, such as "GET" or "POST"; must not be null
     * @param absoluteUrl including query string, such as "https://server.example.com/path/to/service?withQuery=yes"
     * @param fromToken a username or other public identifying token for the client
     * @param nonce in the form sent to the server; hexadecimal form is recommended
     * @param body the request body or empty string if the request does not require a body; must not be null
     * @param signatureMethod the algorithm used to generate the signature, such as "HMAC-SHA1" or "HMAC-SHA256"
     * @param timestamp the time this request was created, in ISO 8601 format such as "2012-02-14T08:15:00-08:00"
     * @return 
     */
    private String signatureBlock(String httpMethod, String absoluteUrl, String fromToken, String nonce, String body, String signatureMethod, String timestamp) {
        String[] input = new String[] { httpMethod,  absoluteUrl,  fromToken,  nonce,  signatureMethod,  timestamp }; // can not be null or empty or contain newlines; body is excluded from this
        String[] label = new String[] {"HttpMethod","AbsoluteUrl","FromToken","Nonce","SignatureMethod","Timestamp"};
        ArrayList<String> errors = new ArrayList<String>();
        for(int i=0; i<input.length; i++) {
            if( input[i] == null ) { errors.add(String.format("%s is null", label[i])); }
            if( input[i].contains("\n") || input[i].contains("\r") ) { errors.add(String.format("%s contains newlines", label[i])); }
        }
        if( body == null ) { body = ""; } // errors.add(String.format("%s is null", "Body"));
        if( !errors.isEmpty() ) { throw new IllegalArgumentException("Cannot create signature block: "+StringUtils.join(errors, ", ")); }
        return String.format("Request: %s %s\nFrom: %s\nTimestamp: %s\nNonce: %s\nSignature-Method: %s\n\n%s", httpMethod, absoluteUrl, fromToken, timestamp, nonce, signatureMethod, body);
    }
    
    public String toString() { return signatureBlock(httpMethod, absoluteUrl, fromToken, nonce, body, signatureMethod, timestamp); }
    
}
