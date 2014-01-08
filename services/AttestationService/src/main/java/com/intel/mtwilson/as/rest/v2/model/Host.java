/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jersey.Document;

/**
 * Example serialization:
 * 
<host>
<id>34b1f684-7f71-48d3-a0a0-41768f9ed130</id>
<name>hostxyz</name>
<connection_url>http://1.2.3.4</connection_url>
<description>test host</description>
<bios_mle>bios-4.3.2</bios_mle>
</host>
 *
 * The JacksonXmlRootElement(localName="host") annotation is responsible
 * for the lowercase "host" tag, otherwise the default would be "Host"
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="host")
public class Host extends Document {
    private String name;
    private String connectionUrl;
    private String description;
    private String biosMLE;
    private String IPAddress;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBiosMLE() {
        return biosMLE;
    }

    public void setBiosMLE(String biosMLE) {
        this.biosMLE = biosMLE;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }
    
    
}
