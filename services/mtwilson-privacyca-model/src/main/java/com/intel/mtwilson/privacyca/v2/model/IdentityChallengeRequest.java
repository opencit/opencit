/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DEROctetString;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="identity_challenge_request")
public class IdentityChallengeRequest {
    private byte[] identityRequest;
    private byte[] endorsementCertificate;
    private byte[] aikName;
    private String tpmVersion;

    public byte[] getAikName() {
        return aikName;
    }

    public void setAikName(byte[] aikName) {
        this.aikName = aikName;
    }

    public String getTpmVersion() {
        return tpmVersion;
    }

    public void setTpmVersion(String tpmVersion) {
        this.tpmVersion = tpmVersion;
    }

    public void setIdentityRequest(byte[] identityRequest) {
        this.identityRequest = identityRequest;
    }

    public void setEndorsementCertificate(byte[] endorsementCertificate) {
        this.endorsementCertificate = endorsementCertificate;
    }

    public byte[] getIdentityRequest() {
        return identityRequest;
    }

    public byte[] getEndorsementCertificate() {
        return endorsementCertificate;
    }
    
//    @JsonIgnore
    public byte[] toByteArray() {
        DEROctetString identityRequestOctets = new DEROctetString(identityRequest);
        DEROctetString endorsementCertificateOctets = new DEROctetString(endorsementCertificate);
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(identityRequestOctets);
        v.add(endorsementCertificateOctets);
        DERSequence sequence = new DERSequence(v);
        return sequence.getDEREncoded();
    }
    
    public static IdentityChallengeRequest valueOf(ASN1Sequence sequence) {
        IdentityChallengeRequest identityChallengeRequest = new IdentityChallengeRequest();
        identityChallengeRequest.identityRequest = DEROctetString.getInstance(sequence.getObjectAt(0)).getOctets();
        identityChallengeRequest.endorsementCertificate = DEROctetString.getInstance(sequence.getObjectAt(1)).getOctets();
        return identityChallengeRequest;
    }
    
    public static IdentityChallengeRequest valueOf(byte[] der) {
        return valueOf(ASN1Sequence.getInstance(der));
    }
}
