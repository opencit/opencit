/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
//import com.intel.dcsg.cpg.validation.Regex;
//import com.intel.dcsg.cpg.validation.RegexPatterns;

/**
 * 
 * @author ssbangal, jbuhacoff
 */
@JacksonXmlRootElement(localName="tls_policy")
public class HostTlsPolicy extends Document {
    
    private String name;
    
//    @JsonProperty("private")
    private boolean privateScope = false;
    
    private TlsPolicyDescriptor descriptor;

    private String comment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    

    public boolean isPrivate() {
        return privateScope;
    }

    public void setPrivate(boolean privateScope) {
        this.privateScope = privateScope;
    }

    public TlsPolicyDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(TlsPolicyDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    

    
    public String getComment() {
        return comment;
    }


    public void setComment(String comment) {
        this.comment = comment;
    }
    
    
}
