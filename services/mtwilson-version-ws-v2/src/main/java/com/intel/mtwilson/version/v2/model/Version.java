/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.version.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="version")
public class Version extends Document {
    private static final com.intel.mtwilson.Version version = com.intel.mtwilson.Version.getInstance(); // com.intel.mtwilson.Version is generated from a java-template in the mtwilson-version project
    
    public String getVersion() { return version.getVersion(); }
    public String getBranch() { return version.getBranch(); }
    public String getTimestamp() { return version.getTimestamp(); }
    
}
