/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.jaxrs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="configuration_collection")
public class ConfigurationDocumentCollection extends DocumentCollection<ConfigurationDocument> {
    private ArrayList<ConfigurationDocument> documents;
    
    @Override
    public List<ConfigurationDocument> getDocuments() {
        return documents;
    }
    
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="configurations")
    @JacksonXmlProperty(localName="configuration")    
    public List<ConfigurationDocument> getConfigurations() {
        return documents;
    }
    
}
