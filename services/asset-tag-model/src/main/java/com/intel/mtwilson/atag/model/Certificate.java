/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class Certificate extends Document {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Certificate.class);

    private byte[] certificate; // variable size
    private Sha1Digest sha1; // 20 bytes      SHA1(CERTIFICATE)  the certificate fingerprint
    private Sha256Digest sha256; // 32 bytes      SHA256(CERTIFICATE)  the certificate fingerprint
//    private byte[] sha1; // 20 bytes   SHA1(CERTIFICATE)  the certificate fingerprint
//    private Sha1Digest pcrEventSha1; // 20 bytes   SHA1(SHA1(CERTIFICATE))   the hash of certificate fingerprint that gets extended to the TPM PCR  because tboot always hashes it even if it's already the right size 
//    private Sha256Digest pcrEventSha256; // 32 bytes   SHA256(SHA256(CERTIFICATE))   the hash of certificate fingerprint that gets extended to the TPM PCR because tboot always hashes it even if it's already the right size 
    private String subject;
    private String issuer;
    private Date notBefore;
    private Date notAfter;
    private boolean revoked = false;
    
    public Certificate() {
    }

    
    public byte[] getCertificate() {
        return certificate;
    }

    public Sha1Digest getSha1() {
        return sha1;
    }

    
    public Sha256Digest getSha256() {
        return sha256;
    }


    public String getSubject() {
        return subject;
    }

    public String getIssuer() {
        return issuer;
    }
    
    

    public Date getNotBefore() {
        return notBefore;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public boolean getRevoked() {
        return revoked;
    }

    
    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public void setSha1(Sha1Digest sha1) {
        this.sha1 = sha1;
    }

    
    public void setSha256(Sha256Digest sha256) {
        this.sha256 = sha256;
    }


    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    
    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    public void setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
    
    
    // you still need to setUuid() after calling this since it's not included in the serialized form
    @JsonCreator
    public static Certificate valueOf(String text) throws UnsupportedEncodingException {
        byte[] data = Base64.decodeBase64(text);
        return valueOf(data);
    }

    // you still need to setUuid() after calling this since it's not included in the serialized form
    public static Certificate valueOf(byte[] data) throws UnsupportedEncodingException {
        Certificate certificate = new Certificate();
        certificate.setCertificate(data);
        certificate.setSha1(Sha1Digest.digestOf(data)); // throws UnsupportedEncodingException
        certificate.setSha256(Sha256Digest.digestOf(data)); // throws UnsupportedEncodingException
//        certificate.setPcrEventSha256(Sha256Digest.digestOf(certificate.getSha256().toByteArray()));
//        certificate.setPcrEventSha1(Sha1Digest.digestOf(certificate.getSha1().toByteArray()));
        X509AttributeCertificate attrcert = X509AttributeCertificate.valueOf(data);
        certificate.setIssuer(attrcert.getIssuer());
        certificate.setSubject(attrcert.getSubject());
        // XXX TODO need to verify the certificate against known ca's before we really believe these validity dates... assuming that will happen where they matter.
        certificate.setNotBefore(attrcert.getNotBefore());
        certificate.setNotAfter(attrcert.getNotAfter());
        // assuming revoked = false (default value)
        log.debug("valueOf ok");
        return certificate;
    }
    
}
