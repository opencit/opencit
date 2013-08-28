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
public class SelectionTagValue {
    // this record links a tag instance (tag id and value id) to a certificate request
    private long id;
    private long selectionId;
    private long tagId;
    private long tagValueId;
    // name, oid, and value are provided as inputs by API users; they are looked up in the database to populate the id fields above
    private String name;
    private String oid;
    private String value;

    public SelectionTagValue() {
    }

    public SelectionTagValue(long id, long selectionId, long tagId, long tagValueId) {
        this.id = id;
        this.selectionId = selectionId;
        this.tagId = tagId;
        this.tagValueId = tagValueId;
    }

    public SelectionTagValue(String name, String oid, String value) {
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
    public long getSelectionId() {
        return selectionId;
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
    public void setSelectionId(long selectionId) {
        this.selectionId = selectionId;
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
