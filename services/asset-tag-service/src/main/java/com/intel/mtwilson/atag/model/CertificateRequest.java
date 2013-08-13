/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intel.dcsg.cpg.io.UUID;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class CertificateRequest {
    private long id;
    private UUID uuid;
    private String subject;
    private List<CertificateRequestTagValue> tags;
    private String status;
    private long certificateId;

    public CertificateRequest() {
    }

    public CertificateRequest(long id, UUID uuid) {
        this.id = id;
        this.uuid = uuid;
//        this.subject = subject;
//        this.tags = tags;
    }
    
    public CertificateRequest(String subject, List<CertificateRequestTagValue> tags) {
//        this.id = id;
//        this.uuid = uuid;
        this.subject = subject;
        this.tags = tags;
    }
    
    public CertificateRequest(long id, UUID uuid, String subject, List<CertificateRequestTagValue> tags) {
        this.id = id;
        this.uuid = uuid;
        this.subject = subject;
        this.tags = tags;
    }

    @JsonIgnore
    public long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getSubject() {
        return subject;
    }

    
    public List<CertificateRequestTagValue> getTags() {
        return tags;
    }

    public String getStatus() {
        return status;
    }

    public long getCertificateId() {
        return certificateId;
    }
    
    

//    @JsonIgnore
    public void setId(long id) {
        this.id = id;
    }

//    @JsonIgnore
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    
    public void setTags(List<CertificateRequestTagValue> tags) {
        this.tags = tags;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCertificateId(long certificateId) {
        this.certificateId = certificateId;
    }


    
}
