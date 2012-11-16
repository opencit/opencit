/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 *
 * @author jbuhacoff
 */
public class RsaCredentialX509 extends RsaCredential {
    private final X509Certificate certificate;
    
    /**
     * Initializes the RsaCredential using the provided private key and X509
     * certificate. The digest of the X509 certificate will be used as the
     * identity. Note this is not the same as the digest of the public key.
     * @param privateKey
     * @param certificate
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException 
     */
    public RsaCredentialX509(PrivateKey privateKey, X509Certificate certificate) throws CertificateEncodingException, NoSuchAlgorithmException {
        super(privateKey, certificate.getEncoded());
        this.certificate = certificate;
    }
    
    public X509Certificate getCertificate() {
        return certificate;
    }

    @Override
    public PublicKey getPublicKey() {
        return certificate.getPublicKey();
    }

}
