/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.file.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.mtwilson.jaxrs2.Document;

/**
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="file")
public class File extends Document {
    private String name;
    private String content;
    
    private String contentType;

    @Regex("(?:[a-zA-Z0-9\\.-]+)")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Regex("(?:[a-zA-Z0-9\\./;\" -]+)")
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
