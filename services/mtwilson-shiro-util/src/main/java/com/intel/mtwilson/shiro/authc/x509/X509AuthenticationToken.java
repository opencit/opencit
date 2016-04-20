/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.x509;

import com.intel.mtwilson.security.http.RsaSignatureInput;
import com.intel.mtwilson.shiro.*;
import java.security.cert.X509Certificate;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * The X509AuthenticationFilter reads the entire request and 
 * checks the Authorization header for the X509 authorization
 * scheme. If present, the Authorization header indicates 
 * which other headers to include in the signature. The 
 * X509AuthenticationFilter recreates the "signed document"
 * and computes its digest. It then creates this
 * X509AuthorizationToken with the fingerprint and signature provided
 * in the Authorization header as well as the independently recomputed
 * digest of the "signed document".  
 * When the token is verified against the X509AuthenticationInfo from
 * the database, the signature is verified using the X509Certificate
 * contained in the X509AuthenticationInfo.
 * 
 * @author jbuhacoff
 */
public class X509AuthenticationToken implements AuthenticationToken {
    private Fingerprint principal;
    private Credential credential;
    private RsaSignatureInput signatureInput;
    private String host;
    
    /*
    public X509AuthenticationToken(Fingerprint principal, Credential credential) {
        this.principal = principal;
        this.credential = credential;
        this.host = null;
    }
    */
    public X509AuthenticationToken(Fingerprint principal, Credential credential, RsaSignatureInput signatureInput, String host) {
        this.principal = principal;
        this.credential = credential;
        this.signatureInput = signatureInput;
        this.host = host;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return credential;
    }

    public RsaSignatureInput getSignatureInput() {
        return signatureInput;
    }

    public String getHost() {
        return host;
    }
    
    
    
}
