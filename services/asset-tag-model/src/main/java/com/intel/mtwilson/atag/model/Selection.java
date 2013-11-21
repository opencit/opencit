/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

//import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intel.dcsg.cpg.io.UUID;
import java.util.List;

/**
 * Represents a selection of tags (and possibly hosts to apply them to)
 * 
 * @author jbuhacoff
 */
public class Selection extends Document {
    private String name;
    private List<SelectionTagValue> tags;
    private List<String> subjects; // hosts (optional)

    public Selection() {
    }

    public Selection(long id, UUID uuid) {
        setId(id);
        setUuid(uuid);
    }
    
    public Selection(long id, UUID uuid, List<String> subjects, List<SelectionTagValue> tags) {
        setId(id);
        setUuid(uuid);
        this.subjects = subjects;
        this.tags = tags;
    }
    
    public Selection(String name) {
        this.name = name;
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
