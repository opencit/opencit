/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.mtwilson.jaxrs2.Document;

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
    private String biosMleUuid;
    private String vmmMleUuid;
    private String email;
    private String aikCertificate;  // may be null
    private String aikPublicKey;  // may be null
    private String aikSha1;  // may be null
    private String tlsPolicyId; // may be null, a uuid reference to mw_tls_policy table, or special keyword INSECURE or TRUST_FIRST_CERTIFICATE
    private String hardwareUuid;

    @Regex(RegexPatterns.IPADDR_FQDN)    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Regex(RegexPatterns.ANY_VALUE)
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

    public String getBiosMleUuid() {
        return biosMleUuid;
    }

    public void setBiosMleUuid(String biosMleUuid) {
        this.biosMleUuid = biosMleUuid;
    }

    public String getVmmMleUuid() {
        return vmmMleUuid;
    }

    public void setVmmMleUuid(String vmmMleUuid) {
        this.vmmMleUuid = vmmMleUuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAikCertificate() {
        return aikCertificate;
    }

    public void setAikCertificate(String aikCertificate) {
        this.aikCertificate = aikCertificate;
    }

    public String getAikPublicKey() {
        return aikPublicKey;
    }

    public void setAikPublicKey(String aikPublicKey) {
        this.aikPublicKey = aikPublicKey;
    }

    public String getAikSha1() {
        return aikSha1;
    }

    public void setAikSha1(String aikSha1) {
        this.aikSha1 = aikSha1;
    }

    public String getTlsPolicyId() {
        return tlsPolicyId;
    }

    public void setTlsPolicyId(String tlsPolicyId) {
        this.tlsPolicyId = tlsPolicyId;
    }

    public String getHardwareUuid() {
        return hardwareUuid;
    }

    public void setHardwareUuid(String hardwareUuid) {
        this.hardwareUuid = hardwareUuid;
    }
    
        
}
