/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intel.dcsg.cpg.io.UUID;
import java.util.List;

/**
 * Represents a selection of tags (and possibly hosts to apply them to)
 * 
 * @author jbuhacoff
 */
public class Selection {
    private long id;
    private UUID uuid;
    private String name;
    private List<SelectionTagValue> tags;
    private List<String> subjects; // hosts (optional)

    public Selection() {
    }

    public Selection(long id, UUID uuid) {
        this.id = id;
        this.uuid = uuid;
    }
    
    public Selection(long id, UUID uuid, List<String> subjects, List<SelectionTagValue> tags) {
        this.id = id;
        this.uuid = uuid;
        this.subjects = subjects;
        this.tags = tags;
    }

    @JsonIgnore
    public long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    
    public List<String> getSubjects() {
        return subjects;
    }

    
    public List<SelectionTagValue> getTags() {
        return tags;
    }


//    @JsonIgnore
    public void setId(long id) {
        this.id = id;
    }

//    @JsonIgnore
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    
    public void setTags(List<SelectionTagValue> tags) {
        this.tags = tags;
    }


    
}
