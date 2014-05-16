/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.v2.model;

import com.intel.mtwilson.jaxrs2.DocumentCollection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="configuration_collection")
public class ConfigurationCollection extends DocumentCollection<Configuration> {
    private final ArrayList<Configuration> files = new ArrayList<Configuration>();
    
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="configurations")
    @JacksonXmlProperty(localName="configuration")    
    public List<Configuration> getConfigurations() { return files; }
    
    @Override
    public List<Configuration> getDocuments() {
        return getConfigurations();
    }
    
    
}
