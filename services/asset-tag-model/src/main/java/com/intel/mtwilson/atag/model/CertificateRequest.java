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
    private String selection; // tags to include in the certificate
    private String status;
    private long certificateId;
    private long selectionId; // set by DAO when loading
    private UUID certificate;

    public CertificateRequest() {
    }

    public CertificateRequest(long id, UUID uuid) {
        this.id = id;
        this.uuid = uuid;
//        this.subject = subject;
//        this.tags = tags;
    }
    
    public CertificateRequest(String subject) {
        this.subject = subject;
    }
    
    public CertificateRequest(String subject, String selection) {
//        this.id = id;
//        this.uuid = uuid;
        this.subject = subject;
        this.selection = selection;
    }
    
    public CertificateRequest(long id, UUID uuid, String subject, String selection) {
        this.id = id;
        this.uuid = uuid;
        this.subject = subject;
        this.selection = selection;
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

    @JsonIgnore
    public long getSelectionId() {
        return selectionId;
    }

    
    public String getSelection() {
        return selection;
    }

    public String getStatus() {
        return status;
    }

    @JsonIgnore
    public long getCertificateId() {
        return certificateId;
    }

    public UUID getCertificate() {
        return certificate;
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

    
    public void setSelection(String selection) {
        this.selection = selection;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCertificateId(long certificateId) {
        this.certificateId = certificateId;
    }

    public void setCertificate(UUID certificate) {
        this.certificate = certificate;
    }
    
    

    public void setSelectionId(long selectionId) {
        this.selectionId = selectionId;
    }


    
}
