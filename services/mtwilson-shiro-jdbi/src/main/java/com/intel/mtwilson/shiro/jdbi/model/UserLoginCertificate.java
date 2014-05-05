/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import java.util.Date;

/**
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  certificate bytea NOT NULL,
  sha1_hash bytea NOT NULL,
  sha256_hash bytea NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'Pending',
  comment text,
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="user_login_certificate")
public class UserLoginCertificate {
    private UUID id;
    private UUID userId;
    private byte[] certificate;
    private byte[] sha1Hash;
    private byte[] sha256Hash;
    private Date expires;
    private boolean enabled;
    private Status status;
    private String comment;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public byte[] getSha1Hash() {
        return sha1Hash;
    }

    public void setSha1Hash(byte[] sha1Hash) {
        this.sha1Hash = sha1Hash;
    }

    public byte[] getSha256Hash() {
        return sha256Hash;
    }
    
    

    public void setSha256Hash(byte[] sha256Hash) {
        this.sha256Hash = sha256Hash;
    }

    

    
    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    
    
}
