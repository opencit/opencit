/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

//import com.intel.dcsg.cpg.io.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * 
 * @author jbuhacoff
 */
public class CertificateRequestTagValue {
    // this record links a tag instance (tag id and value id) to a certificate request
    private long id;
    private long certificateRequestId;
    private long tagId;
    private long tagValueId;
    // name, oid, and value are provided as inputs by API users; they are looked up in the database to populate the id fields above
    private String name;
    private String oid;
    private String value;

    public CertificateRequestTagValue() {
    }

    public CertificateRequestTagValue(long id, long certificateRequestId, long tagId, long tagValueId) {
        this.id = id;
        this.certificateRequestId = certificateRequestId;
        this.tagId = tagId;
        this.tagValueId = tagValueId;
    }

    public CertificateRequestTagValue(String name, String oid, String value) {
//        this.id = id;
        this.name = name;
        this.oid = oid;
        this.value = value;
    }
    
    @JsonIgnore
    public long getId() {
        return id;
    }

    @JsonIgnore
    public long getCertificateRequestId() {
        return certificateRequestId;
    }

    @JsonIgnore
    public long getTagId() {
        return tagId;
    }

    @JsonIgnore
    public long getTagValueId() {
        return tagValueId;
    }

    public String getName() {
        return name;
    }

    public String getOid() {
        return oid;
    }

    public String getValue() {
        return value;
    }
    
    

    @JsonIgnore
    public void setId(long id) {
        this.id = id;
    }

    @JsonIgnore
    public void setCertificateRequestId(long certificateRequestId) {
        this.certificateRequestId = certificateRequestId;
    }

    @JsonIgnore
    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    @JsonIgnore
    public void setTagValueId(long tagValueId) {
        this.tagValueId = tagValueId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public void setValue(String value) {
        this.value = value;
    }


    
}
