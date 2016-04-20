/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intel.mtwilson.jackson.PublicKeyDeserializer;
import com.intel.mtwilson.jackson.PublicKeySerializer;
import com.intel.mtwilson.jackson.X509CertificateDeserializer;
import com.intel.mtwilson.jackson.X509CertificateSerializer;

/**
 * draft
 * @author jbuhacoff
 */
public class Aik {
    
    @JsonSerialize(using=PublicKeySerializer.class)
    @JsonDeserialize(using=PublicKeyDeserializer.class)
    private PublicKey publicKey;
    
    @JsonSerialize(using=X509CertificateSerializer.class)
    @JsonDeserialize(using=X509CertificateDeserializer.class)
    private X509Certificate certificate;
    
    protected Aik() { }   // for desearializing jackson
    
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
    
    protected void setPublicKey(PublicKey publicKey) {
        this.certificate = null;
        this.publicKey = publicKey;
    }
    
    protected void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
        this.publicKey = certificate.getPublicKey();
    }
}
