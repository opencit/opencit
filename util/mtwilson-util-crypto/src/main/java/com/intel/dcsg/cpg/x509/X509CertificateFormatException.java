/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509;

/**
 * Thrown when a byte array cannot be decoded to an X509Certificate instance. 
 *
 * @since 0.2
 * @author jbuhacoff
 */
public class X509CertificateFormatException extends IllegalArgumentException {
    private byte[] certificate;
    public X509CertificateFormatException(byte[] certificate) {
        super();
        this.certificate = certificate;
    }
    public X509CertificateFormatException(Throwable cause, byte[] certificate) {
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
    
    public byte[] getBytes() {
        return certificate;
    }
}
