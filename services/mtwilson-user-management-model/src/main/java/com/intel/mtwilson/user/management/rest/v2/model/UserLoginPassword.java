/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.crypto.password.HashProtection;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.Date;
import java.util.List;

/**
 *  salt should be 8 bytes long minimum recommended in PKCS5 standard
 * 
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  password_hash bytea NOT NULL,
  salt bytea NOT NULL,
  iterations integer DEFAULT 1,
  algorithm character varying(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="user_login_password")
public class UserLoginPassword extends Document implements HashProtection {
    private UUID id;
    private UUID userId;
    private byte[] passwordHash;
    private byte[] salt;
    private int iterations;
    private String algorithm;
    private Date expires;
    private boolean enabled;
    private Status status;
    private String comment;
    private List<String> roles;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    @Override
    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
}
