/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.http;

import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * This class requires the following libraries:
 * org.apache.commons.lang.StringUtils from commons-lang
 * 
 * This class defines the format of the content which is hashed to produce
 * the RSA asymmetric-key signature for the "PublicKey" or "X509" authentication
 * scheme. This class could probably also
 * be used with other signature algorithms. The structure of the document
 * to sign is different between MtWilson and PublicKey authentication schemes.
 * 
 * This class allows the inclusion of arbitrary headers (including nonce and
 * timestamp) into the signed document, giving the HTTP Agent and Web Service a 
 * lot of flexibility in determining just what should or shouldn't be included
 * in the signed document, without duplicating information from standard
 * HTTP headers into the Authorization header.
 * 
 * Mt Wilson 2.0 clients MUST include the Date header in the request
 * and cover it with the signature.
 * 
 * @since 0.5.2
 * @author jbuhacoff
 */
public class RsaSignatureInput {
  
    public String httpMethod; // the HTTP request method, such as "GET" or "POST"; must not be null
    public String url; // including query string if it is intended to be signed, such as "https://server.example.com/path/to/service?withQuery=yes"; it does not have to be absolute, but it does need to match the HTTP Request Line or an X-Original-Request or X-Original-URL header (that substitution should be done OUTSIDE of this class, so the url passed should be the one to be validated)
    public String realm; // the authentication realm (must be agreed upon between server and client)
    public String fingerprintBase64; // the base-64 encoded fingerprint that identifies the public key or X509 certificate that should be used to verify the signature
    public String signatureAlgorithm; // the signature algorithm; currently must be "SHA256withRSA" or "RSA-SHA256" 
    public String[] headerNames; // headers that should be included in the signature, in order
    public Map<String,String> headers; // a map that contains at least the headers referenced in headerNames; headers not referenced will be ignored
    public String body; // the request body or empty string if the request does not require a body; must not be null
    
    /**
     * Generates the request document to be signed. This document represents the
     * request because it includes the Http method, request URL, user-id token,
     * selected HTTP headers, signature algorithm.
     * 
     * Any parameters that are null will be represented as an empty string in the signed document.
     * 
     * Sample output:
     * 
Request: POST http://example.com/some/path
Realm: Attestation
From: fingerprint
Signature-Algorithm: RSA-SHA256
X-Date: 2012-02-14T08:15:00-08:00
X-Nonce: FaaKLOOuyG7/kLVD5vQ7iw==

{"numbers":[1,2,3],"notes":"whatever the payload of the request, whether it's plain text, xml, json, or binary file in text encoding"}
     * 
     * @return 
     */
    private String document() {
        String preamble = String.format("Request: %s %s\nRealm: %s\nFrom: %s\nSignature-Algorithm: %s\n",
            valueWithoutNewlinesOrEmptyString(httpMethod), 
            valueWithoutNewlinesOrEmptyString(url),
            valueWithoutNewlinesOrEmptyString(realm),
            valueWithoutNewlinesOrEmptyString(fingerprintBase64),
            valueWithoutNewlinesOrEmptyString(signatureAlgorithm));
        ArrayList<String> httpHeaderList = new ArrayList<>();
        for(String headerName : headerNames) {
            httpHeaderList.add( String.format("%s: %s\n", headerName, valueWithoutNewlinesOrEmptyString(headers.get(headerName))) );
        }
        String httpHeaders = StringUtils.join(httpHeaderList, "");
        return String.format("%s%s\n%s", preamble, httpHeaders, valueOrEmptyString(body));
    }
    
    /**
     * This method protects against tampering with the signed document by 
     * preventing the use of newlines in values that are not supposed to have
     * newlines. Only the the HTTP request body is allowed to have newlines.
     * All other newlines must be encoded before being used in the document.
     * Any value that is null or contains a newline will be returned as an
     * empty string, which will probably cause the signature verification to fail
     * since it won't match the original document.
     * 
     * @param value
     * @return 
     */
    private String valueWithoutNewlinesOrEmptyString(String value) {
        if( value == null ) { return ""; }
        if( value.contains("\n") || value.contains("\r") ) { return ""; }
        return value;
    }
    
    private String valueOrEmptyString(String value) {
        if( value == null ) { return ""; }
        return value;
    }
    
    @Override
    public String toString() { return document(); }
    
}
