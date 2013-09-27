/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intel.dcsg.cpg.io.UUID;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class Configuration extends Document {
    private static Logger log = LoggerFactory.getLogger(Configuration.class);
    private String name;
//    private String contentType;
    private Properties content;
    private static final ObjectMapper json = new ObjectMapper();
    private static final XmlMapper xml = new XmlMapper(); 
    
    public Configuration() {
    }

    public Configuration(long id, UUID uuid) {
        setId(id);
        setUuid(uuid);
    }
    
    public Configuration(String name, Properties content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }
/*
    public String getContentType() {
        return contentType;
    }
*/

    public Properties getContent() {
        return content;
    }
    
    public void setContent(Properties content) {
        this.content = content;
    }
    
    @JsonIgnore
    public String getJsonContent() throws IOException {
        return json.writeValueAsString(content);
    }
    
    @JsonIgnore
    public String getXmlContent() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        content.storeToXML(out, name);
        return out.toString();
    }
    
    public void setName(String name) {
        this.name = name;
    }
/*
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
*/
    @JsonIgnore
    public void setJsonContent(JsonNode content) throws IOException {
        if( content != null && content.isObject() ) {
            setJsonContent(json.writeValueAsString(content));
        }
    }
    
    @JsonIgnore
    public void setJsonContent(String jsonContent) throws IOException {
        content = json.readValue(jsonContent, Properties.class);
    }
    
    @JsonIgnore
    public void setXmlContent(String xmlContent) throws IOException {
        Properties p = new Properties();
        p.loadFromXML(new ByteArrayInputStream(xmlContent.getBytes()));
        content = p; // only set it if we were able to deserialize;  if there is an error during loadFromXml then we retain the previous content
    }
    
    ///////////////////////////// specific configuration settings
    
    private boolean getBoolean(String key, boolean defaultValue) {
        if( content == null ) { return defaultValue; }
        String value = content.getProperty(key);
        if( value == null ) { return defaultValue; }
        return Boolean.valueOf(value);
    }

    private String getString(String key, String defaultValue) {
        if( content == null ) { return defaultValue; }
        return content.getProperty(key, defaultValue);
    }
    
    // secure default values: 
    // don't allow tags in certificate requests, don't allow automatic tag selection, 
    // and disable automatic approval means nobody is getting an asset tag until the 
    // system administrator configures the service
    
    @JsonIgnore
    public boolean isAllowTagsInCertificateRequests() {
        return getBoolean("allowTagsInCertificateRequests", false);
    }

    @JsonIgnore
    public boolean isAllowAutomaticTagSelection() {
        return getBoolean("allowAutomaticTagSelection", false);
    }

    @JsonIgnore
    public String getAutomaticTagSelectionName() {
        return getString("automaticTagSelectionName", "default");
    }
    
    @JsonIgnore
    public boolean isApproveAllCertificateRequests() {
        return getBoolean("approveAllCertificateRequests", false);
    }
    
    // mtwilson url and api client keystore username & password
    @JsonIgnore
    public String getMtWilsonURL() {
        return getString("mtwilsonUrl", null);
    }
    @JsonIgnore
    public String getMtWilsonClientKeystoreUsername() {
        return getString("mtwilsonClientKeystoreUsername", null);
    }
    @JsonIgnore
    public String getMtWilsonClientKeystorePassword() {
        return getString("mtwilsonClientKeystorePassword", null);
    }
    
}
