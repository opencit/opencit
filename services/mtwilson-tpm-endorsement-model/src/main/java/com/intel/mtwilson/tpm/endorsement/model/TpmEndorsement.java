/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tpm.endorsement.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.mtwilson.jaxrs2.Document;

/**
 * Represents a single row in the mw_tpm_endorsement table.
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="tpm_endorsement")
public class TpmEndorsement extends Document {
//   private UUID id;
   private String hardwareUuid;
   @Regex(RegexPatterns.ANY_VALUE)
   private String issuer;
   private boolean revoked;
   private byte[] certificate;
   private String comment;

    public TpmEndorsement() {
    }

    /*
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    */

    public String getHardwareUuid() {
        return hardwareUuid;
    }

    public void setHardwareUuid(String hardwareUuid) {
        this.hardwareUuid = hardwareUuid;
    }
    
    @Regex(RegexPatterns.ANY_VALUE)
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
    
    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
   
   
}
