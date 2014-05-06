/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;

/**
  id uuid NOT NULL,
  user_id uuid NOT NULL,
  keystore bytea NOT NULL,
  keystore_format character varying(128) NOT NULL DEFAULT 'jks',
  comment text DEFAULT NULL,
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="user_keystore")
public class UserKeystore {
    private UUID id;
    private UUID userId;
    private byte[] keystore;
    private String keystoreFormat;
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

    public byte[] getKeystore() {
        return keystore;
    }

    public void setKeystore(byte[] keystore) {
        this.keystore = keystore;
    }

    public String getKeystoreFormat() {
        return keystoreFormat;
    }

    public void setKeystoreFormat(String keystoreFormat) {
        this.keystoreFormat = keystoreFormat;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    
}
