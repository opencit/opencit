/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.util.ByteArray;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
//import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * When using Jackson to serialize a Document subclass, the default behavior
 * is to omit null or empty fields. When using Jackson to de-serialize a
 * Document subclass, the default behavior is to ignore unknown fields. 
 * The combination is intended to facilitate backward-compatible future changes
 * in the API.
 * 
 * @author jbuhacoff
 */
//@JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY) // jackson 1.9
@JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
@JsonIgnoreProperties(ignoreUnknown=true)
public abstract class Document {
    private UUID id;
    private URL href;
    private final HashMap<String,Object> meta = new HashMap<>();
    private final HashMap<String,Object> links = new HashMap<>();
    private String etag;
    private Date createdOn; 
    private Date modifiedOn;
    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public URL getHref() {
        return href;
    }
    
    public void setHref(URL href) {
        this.href = href;
    }
    
    
    public Map<String, Object> getMeta() {
        return meta;
    }

    public Map<String, Object> getLinks() {
        return links;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    

    public String getEtag() {
        if( etag != null ) { return etag; }
        if( modifiedOn != null ) {
            String hex = Long.toHexString(modifiedOn.getTime());
            ByteArray byteArray = ByteArray.fromHex(hex);
            Sha1Digest digest = Sha1Digest.digestOf(byteArray.getBytes());
            return digest.toHexString();
        }
        return null;
    }
}
