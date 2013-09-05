/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

//import com.intel.dcsg.cpg.io.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intel.dcsg.cpg.io.UUID;


/**
 * 
 * @author jbuhacoff
 */
public class SelectionTagValue extends Document {
    // this record links a tag instance (tag id and value id) to a certificate request
    private long selectionId;
    private long tagId;
    private long tagValueId;
    // name, oid, and value are provided as inputs by API users; they are looked up in the database to populate the id fields above
    private UUID tagUuid;
    private String tagName;
    private String tagOid;
    private String tagValue;

    public SelectionTagValue() {
    }

    public SelectionTagValue(long id, long selectionId, long tagId, long tagValueId) {
        setId(id);
        this.selectionId = selectionId;
        this.tagId = tagId;
        this.tagValueId = tagValueId;
    }

    public SelectionTagValue(String name, String oid, String value) {
//        this.id = id;
        this.tagName = name;
        this.tagOid = oid;
        this.tagValue = value;
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

    public UUID getTagUuid() {
        return tagUuid;
    }

    
    public String getTagName() {
        return tagName;
    }

    public String getTagOid() {
        return tagOid;
    }

    public String getTagValue() {
        return tagValue;
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

    public void setTagUuid(UUID tagUuid) {
        this.tagUuid = tagUuid;
    }

    
    public void setTagName(String name) {
        this.tagName = name;
    }

    public void setTagOid(String oid) {
        this.tagOid = oid;
    }

    public void setTagValue(String value) {
        this.tagValue = value;
    }



    
}
