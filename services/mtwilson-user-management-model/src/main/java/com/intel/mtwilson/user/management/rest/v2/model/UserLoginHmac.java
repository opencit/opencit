/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.Date;

/**
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  hmac_key bytea NOT NULL,
  protection character varying(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="user_login_hmac")
public class UserLoginHmac extends Document {
    private UUID id;
    private UUID userId;
    private byte[] hmacKey;
    private String protection;
    private Date expires;
    private boolean enabled;

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

    public byte[] getHmacKey() {
        return hmacKey;
    }

    public void setHmacKey(byte[] hmacKey) {
        this.hmacKey = hmacKey;
    }

    public String getProtection() {
        return protection;
    }

    public void setProtection(String protection) {
        this.protection = protection;
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
    
    
}
