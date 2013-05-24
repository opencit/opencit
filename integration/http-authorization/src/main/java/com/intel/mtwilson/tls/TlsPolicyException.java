/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls;

import java.security.cert.X509Certificate;

/**
 * Similar to UnknownCertificateException but extends RuntimExceptino so it can be used from TlsPolicyManager,
 * and includes TlsPolicy and remote address information
 * @author jbuhacoff
 */
public class TlsPolicyException extends RuntimeException {

    private String address;
    private X509Certificate[] chain;
    private TlsPolicy tlsPolicy;

    public TlsPolicyException(String message, String address, TlsPolicy tlsPolicy, X509Certificate[] chain) {
        super(String.format("%s: %s", message, address));
        this.address = address;
        this.tlsPolicy = tlsPolicy;
        this.chain = chain;
    }
    

    public String getAddress() {
        return address;
    }
    
    /**
     * 
     * @return the tls policy that rejected the server certificate
     */
    public TlsPolicy getTlsPolicy() {
        if(tlsPolicy == null){
            return null;
        }
        return tlsPolicy;
    }
    
    /**
     * 
     * @return the server certificate (part of the chain)
     */
    public X509Certificate getCertificate() {
        if (chain == null || chain.length == 0) {
            return null;
        }
        return chain[0];
    }

    /**
     * 
     * @return the entire chain that was passed in, starting with the server certificate
     */
    public X509Certificate[] getCertificateChain() {
        if (chain == null || chain.length == 0) {
            return null;
        }
        return chain;
    }

    
}
