/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
//import com.intel.dcsg.cpg.validation.Regex;
//import com.intel.dcsg.cpg.validation.RegexPatterns;

/**
 * TODO:  move to tls policy ws ...
 * 
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="tls_policy")
public class HostTlsPolicy extends Document {
    
    private String name;
    
//    @JsonProperty("private")
    private boolean privateScope = false;
    
    private String contentType; // application/json; charset=utf-8
    private byte[] content;
    private String comment;
/*
//    private String hostUuid;
//    private Boolean insecure = false;
//    private String[] certificates = null;;
    private byte[] keyStore;
    public String getHostUuid() {
        return hostUuid;
    }

    public void setHostUuid(String hostUuid) {
        this.hostUuid = hostUuid;
    }
*/    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /*public Boolean getInsecure() {
    return insecure;
    }
    public void setInsecure(Boolean insecure) {
    this.insecure = insecure;
    }
    // TODO  need to use a regex for base64
    @Regex(RegexPatterns.ANY_VALUE)
    public String[] getCertificates() {
    return certificates;
    }
    public void setCertificates(String[] certificates) {
    this.certificates = certificates;
    }*/
    /*
    public byte[] getKeyStore() {
    return keyStore;
    }
    public void setKeyStore(byte[] keyStore) {
    this.keyStore = keyStore;
    }
     */ 
    public boolean isPrivate() {
        return privateScope;
    }

    public void setPrivate(boolean privateScope) {
        this.privateScope = privateScope;
    }
    
    
    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public String getComment() {
        return comment;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    
}
