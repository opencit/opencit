/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.model;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intel.mtwilson.jackson.PublicKeySerializer;
import com.intel.mtwilson.jackson.X509CertificateSerializer;

/**
 * XXX draft
 * @author jbuhacoff
 */
public class Aik {
    
    @JsonSerialize(using=PublicKeySerializer.class)
    private PublicKey publicKey;
    
    @JsonSerialize(using=X509CertificateSerializer.class)
    private X509Certificate certificate;
    
    public Aik(PublicKey publicKey) {
        this.certificate = null;
        this.publicKey = publicKey;
    }
    
    public Aik(X509Certificate certificate) {
        this.certificate = certificate;
        this.publicKey = certificate.getPublicKey();
    }
    
    public PublicKey getPublicKey() { return publicKey; }
    public X509Certificate getCertificate() { return certificate; }
    
}
