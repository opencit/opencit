/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.intel.dcsg.cpg.io.UUID;

/**
 *
 * @author jbuhacoff
 */
public class RdfTriple {
    private long id;
    private UUID uuid;
    private String subject;    // model object
    private String predicate;  // attribute
    private String object;     // attribute value

    public RdfTriple() {
    }

    public RdfTriple(String subject, String predicate, String object) {
//        this.id = 0;
//        this.uuid = uuid;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }
    
    public RdfTriple(long id, UUID uuid, String subject, String predicate, String object) {
        this.id = id;
        this.uuid = uuid;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getSubject() {
        return subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public void setObject(String object) {
        this.object = object;
    }


}
