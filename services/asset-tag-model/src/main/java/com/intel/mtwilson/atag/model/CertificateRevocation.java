/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import java.util.UUID;

/**
 *
 * @author jbuhacoff
 */
public class CertificateRevocation {
    private long id;
    private UUID uuid;
    private byte[] sha1; // of the certificate being revoked
    private String status;

    public CertificateRevocation() {
    }

    public CertificateRevocation(long id, UUID uuid, byte[] sha1, String status) {
        this.id = id;
        this.uuid = uuid;
        this.sha1 = sha1;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public byte[] getSha1() {
        return sha1;
    }

    public String getStatus() {
        return status;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setSha1(byte[] sha1) {
        this.sha1 = sha1;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    
    
}
