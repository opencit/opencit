/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 *
 * @author jbuhacoff
 */
public class UnknownCertificateException extends CertificateException {

    private X509Certificate[] chain;

    public UnknownCertificateException(String message) {
        super(message);
        chain = null;
    }

    /**
     * 
     * @param message
     * @param chain of certificates, starting with the server certificate in the first element (required) and then each subsequent certificate is optional and is the CA that signed the previous certificate in the chain
     */
    public UnknownCertificateException(String message, X509Certificate[] chain) {
        super(message);
        this.chain = chain;
    }

    /**
     * Convenience method for when only a single certificate is presented without any CA's (typically it would be self-signed but not necessarily)
     * @param message
     * @param certificate 
     */
    public UnknownCertificateException(String message, X509Certificate certificate) {
        super(message);
        this.chain = new X509Certificate[]{certificate};
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
