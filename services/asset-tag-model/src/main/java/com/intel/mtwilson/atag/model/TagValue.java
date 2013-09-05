/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.intel.dcsg.cpg.io.UUID;

/**
 * XXX  too similar to AttributeOidAndValue... both of these should simply be combined into "Tag" which is a pair
 * of (oid,value). we do not need a class to model the relationship between tag definition and its list of values
 * because that relationship is already modeled in the class (currently named "Tag" but maybe rename to "TagDefinition")
 * 
 * @author jbuhacoff
 */
public class TagValue {
    private long id;
    private UUID uuid;
    private long tagId;
    private String value;

    public TagValue() {
    }

    public TagValue(long id, long tagId, String value) {
        this.id = id;
        this.tagId = tagId;
        this.value = value;
    }
    
    public TagValue(long id, UUID uuid, long tagId, String value) {
        this.id = id;
        this.uuid = uuid;
        this.tagId = tagId;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }
    
    

    public long getTagId() {
        return tagId;
    }

    public String getValue() {
        return value;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    
    

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
    
}
