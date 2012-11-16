/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.ta.data.daa.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author jbuhacoff
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "encoding",
    "content"
})
@XmlRootElement(name = "daa_response")
public class DaaResponse {
    
    @XmlElement(required = true)
    private String content;

    @XmlElement(required = true)
    private String encoding;
    
    public void setContent(String content) {
        this.content = content;
    }
    public String getContent() { return content; }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public String getEncoding() { return encoding; }

}
