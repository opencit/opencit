/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.intel.dcsg.cpg.io.UUID;

/**
 * XXX TODO  Probably don't need this  now that certificates are posted directly as an approval. 
 * @deprecated
 * @author jbuhacoff
 */
public class CertificateRequestApproval {
    private long id;
    private long certificateRequestId;
    private String authorityName;

    public CertificateRequestApproval() {
    }

    public CertificateRequestApproval(long id, long certificateId, boolean approved, String authorityName) {
        this.id = id;
        this.certificateRequestId = certificateId;
//        this.approved = approved;
        this.authorityName = authorityName;
    }

    public long getId() {
        return id;
    }

    public long getCertificateRequestId() {
        return certificateRequestId;
    }

    
    public boolean isApproved() {
        return true; //approved;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    
    public void setId(long id) {
        this.id = id;
    }

    public void setCertificateRequestId(long certificateRequestId) {
        this.certificateRequestId = certificateRequestId;
    }

    
    public void setApproved(boolean approved) {
//        this.approved = approved;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }
    
    
    
}
