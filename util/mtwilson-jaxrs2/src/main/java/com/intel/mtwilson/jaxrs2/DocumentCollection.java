/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The "links" on a collection could be to prev/next pages in a paginated collection
 * The "linked" section is for pre-loading documents; for example a subclass 
 * called HostCollection would have a List<Host> getHosts() method and each host
 * might refer to an MLE by ID and the complete MLE object would be included in
 * the collection's "linked" section so the client doesn't need to make an
 * additional request for every reference - most or all can be pre-loaded and
 * provided in the "linked" section.
 * @author jbuhacoff
 */
//@JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY) // jackson 1.9
@JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
public abstract class DocumentCollection<T> {
    private final HashMap<String,Object> meta = new HashMap<String,Object>();
    private final HashMap<String,Object> links = new HashMap<String,Object>();
    private final HashMap<String,Object> linked = new HashMap<String,Object>();

    public Map<String, Object> getMeta() {
        return meta;
    }

    public Map<String, Object> getLinks() {
        return links;
    }
    
    public Map<String, Object> getLinked() {
        return linked;
    }

//    @org.codehaus.jackson.annotate.JsonIgnore // jackson 1.x
    @com.fasterxml.jackson.annotation.JsonIgnore // jackson 2.x
    public abstract List<T> getDocuments();
}
