/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import java.util.Date;

/**
 *
 * @author jbuhacoff
 */
public class RequestLogEntry {
    private String instance;
    private Date received;
    private String source;
    private String content;
    private String digest;

    public RequestLogEntry() {
    }

    public RequestLogEntry(String instance, Date received, String source, String content, String digest) {
        this.instance = instance;
        this.received = received;
        this.source = source;
        this.content = content;
        this.digest = digest;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getContent() {
        return content;
    }

    public String getDigest() {
        return digest;
    }

    public String getInstance() {
        return instance;
    }

    public Date getReceived() {
        return received;
    }

    public String getSource() {
        return source;
    }
    
    
}
