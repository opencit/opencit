/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.version.v2.model;

import com.intel.mtwilson.jaxrs2.DocumentCollection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Not clear yet if we'll offer a search capability that would possibly
 * return multiple RPC requests, but implementing for now to stay consistent
 * with the other resource APIs.
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="version_collection")
public class VersionCollection extends DocumentCollection<Version> {
    private final ArrayList<Version> files = new ArrayList<Version>();
    
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="versions")
    @JacksonXmlProperty(localName="version")    
    public List<Version> getVersions() { return files; }
    
    @Override
    public List<Version> getDocuments() {
        return getVersions();
    }
    
    
}
