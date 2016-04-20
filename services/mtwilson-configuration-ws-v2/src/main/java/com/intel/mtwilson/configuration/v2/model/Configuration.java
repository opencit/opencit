/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.v2.model;

import com.intel.mtwilson.configuration.v2.model.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.HashMap;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="configuration")
public class Configuration extends Document {
    
    private String name;
    private HashMap<String,String> properties = new HashMap<String,String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }
    
    
    
}
