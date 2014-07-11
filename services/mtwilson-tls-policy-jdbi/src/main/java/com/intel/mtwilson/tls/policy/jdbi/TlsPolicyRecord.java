/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.jdbi;

import com.intel.dcsg.cpg.io.UUID;

/**
 * Represents a single row in the mw_tls_policy table.
 * @author jbuhacoff
 */
public class TlsPolicyRecord {
   private UUID id;
   private String name;
   private boolean privateScope;
   private String contentType;
   private byte[] content;
   private String comment;

    public TlsPolicyRecord() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPrivate() {
        return privateScope;
    }

    public void setPrivate(boolean privateScope) {
        this.privateScope = privateScope;
    }
    
   public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
   
   
}
