/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.dcsg.cpg.io.UUID;
import java.net.URL;
import java.util.HashMap;

/**
 * A base class for resources that are handled by the API. 
 * When a document is used for output, typically the id and uuid fields
 * will be set but we do not send the id field to the clients because
 * the id field is for internal use only, but the uuid field is public.
 * The links field is provided because many documents need to link to
 * other documents. The JsonInclude annotation hides the uuid field when
 * its null. 
 * 
 * There is a MultivaluedMapImpl in com.intel.mtwilson in api-client-jar
 * which could be used for the links field instead of HashMap if we need
 * to support multiple URLs per relation. This class is purposefully
 * missing a setLinks() method for two reasons: 1) forward compatibility
 * with a possible MultivaluedMap implementation, and 2) prevent the
 * possibility of someone setting the links field to null so that code
 * that needs to add links can assume the links field is never null.
 * 
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Document {
    @JsonIgnore private long id;
    private UUID uuid;
    private HashMap<String,URL> links; // don't set it so it will be omitted when empty...   = new HashMap<String,URL>();

    public Document() {
    }

    public long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    /**
     * Mainly for serializing. Avoid writing getLinks().put(rel,URL) -- instead,
     * use addLinks(rel,URL).  If you need to clear the links then writing
     * getLinks().clear() is ok.
     * @return 
     */
    public HashMap<String, URL> getLinks() {
        return links;
    }
    
    

    public void setId(long id) {
        this.id = id;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * The addLink method is the preferred way to add links.
     * @param rel
     * @param link 
     */
    public void addLink(String rel, URL link) {
        if( links == null ) {
            links = new HashMap<String,URL>();
        }
        links.put(rel, link);
    }

    public void removeLink(String rel) {
        if( links != null ) {
            links.remove(rel);
        }
    }
}
