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
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class Certificate {
    private long id;
    private UUID uuid;
    private byte[] certificate; // variable size
    private Sha256Digest sha256; // 32 bytes      SHA256(CERTIFICATE)  the certificate fingerprint
//    private byte[] sha1; // 20 bytes   SHA1(CERTIFICATE)  the certificate fingerprint
    private Sha1Digest pcrEvent; // 20 bytes   SHA1(SHA256(CERTIFICATE))   the hash of certificate fingerprint that gets extended to the TPM PCR

    public Certificate() {
    }

    public Certificate(long id, UUID uuid, byte[] certificate, Sha256Digest sha256, Sha1Digest pcrEvent) {
        this.id = id;
        this.uuid = uuid;
        this.certificate = certificate;
        this.sha256 = sha256;
        this.pcrEvent = pcrEvent;
    }
    
    public long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    
    public byte[] getCertificate() {
        return certificate;
    }

    public Sha256Digest getSha256() {
        return sha256;
    }

    

    public Sha1Digest getPcrEvent() {
        return pcrEvent;
    }

    
    public void setId(long id) {
        this.id = id;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public void setSha256(Sha256Digest sha256) {
        this.sha256 = sha256;
    }

    

    public void setPcrEvent(Sha1Digest pcrEvent) {
        this.pcrEvent = pcrEvent;
    }
    
    
    @JsonCreator
    public static Certificate valueOf(String text) throws UnsupportedEncodingException {
        byte[] data = Base64.decodeBase64(text);
        Certificate certificate = new Certificate();
        certificate.setCertificate(data);
        certificate.setSha256(Sha256Digest.digestOf(text.getBytes("UTF-8"))); // throws UnsupportedEncodingException
        certificate.setPcrEvent(Sha1Digest.digestOf(certificate.getSha256().toByteArray()));
        return certificate;
    }
    
}
