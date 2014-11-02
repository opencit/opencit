/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509;

import java.security.cert.X509Certificate;

/**
 * Thrown when an X509Certificate instance cannot be encoded to bytes 
 * via getEncoded()
 * 
 * @since 0.2
 * @author jbuhacoff
 */
public class X509CertificateEncodingException extends IllegalArgumentException {
    private X509Certificate certificate;
    public X509CertificateEncodingException(X509Certificate certificate) {
        super();
        this.certificate = certificate;
    }
    public X509CertificateEncodingException(Throwable cause, X509Certificate certificate) {
        super(cause);
        this.certificate = certificate;
    }

    /*
    public X509CertificateFormatException(String message) {
    super(message);
    }
    public X509CertificateFormatException(String message, Throwable cause) {
    super(message, cause);
    }
     */
    
    public X509Certificate getCertificate() {
        return certificate;
    }
}
