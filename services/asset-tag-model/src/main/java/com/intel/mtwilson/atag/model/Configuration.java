/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intel.dcsg.cpg.io.UUID;
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
    private ContentType contentType;
    private String content;
    private JsonNode jsonContent;
    private static final ObjectMapper json = new ObjectMapper();
    private static final XmlMapper xml = new XmlMapper(); 
    
    public Configuration() {
    }

    public Configuration(long id, UUID uuid) {
        setId(id);
        setUuid(uuid);
    }
    
    public Configuration(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }


    public String getContent() {
        log.debug("getContent");
        return content;
    }
    
    public JsonNode getJsonContent() {
        log.debug("getJsonContent");
        if( jsonContent == null ) {
            if( content != null ) {
                if( ContentType.JSON.equals(contentType)) {
                    try {
                        jsonContent = json.readTree(content);
                    }
                    catch(IOException e) {
                        log.warn("Cannot deserialize JSON content", e);
                        jsonContent = null;
                    }
                }
            }
        }
        return jsonContent;
    }
    
    @JsonIgnore
    public JsonNode getXmlContent() {
        try {
            if( ContentType.XML.equals(contentType)) {
                return xml.readTree(content);
            }
            else {
                return null;
            }
        }
        catch(IOException e) {
            log.warn("Cannot deserialize XML content", e);
            return null;
        }
    }
    
    @JsonIgnore
    public Properties getPropertiesContent() {
        try {
            if( ContentType.PROPERTIES.equals(contentType)) {
                Properties p = new Properties();
                p.load(new StringReader(content));
                return p;
            }
            else {
                return null;
            }
        }
        catch(IOException e) {
            log.warn("Cannot deserialize Java Properties content", e);
            return null;
        }
    }
    

    public ContentType getContentType() {
        return contentType;
    }
    
    

    public void setName(String name) {
        this.name = name;
    }

    public void setContent(String content) {
        this.content = content;
        this.jsonContent = null; // will be populated next getJsonContent() is called
        log.debug("setContent: {}", this.content);
    }

    public void setJsonContent(JsonNode content) throws JsonProcessingException {
        if( content != null && content.isObject() ) {
            log.debug("setJsonContent called with object");
            this.content = json.writeValueAsString(content);        
            log.debug("setJsonContent: {}", this.content);
        }
    }
    
    public void setContentType(ContentType contentType) {
        log.debug("setContentType: {}", contentType.name());
        this.contentType = contentType;
    }
    
    public static enum ContentType {
        TEXT, JSON, XML, YAML, PROPERTIES;
    }
    
    ///////////////////////////// specific configuration settings
    
    private boolean getBoolean(String key, boolean defaultValue) {
        if( content != null ) {
            JsonNode root = getJsonContent();
            JsonNode node = root.get(key);
            if( node != null && node.isBoolean() ) {
                return node.booleanValue();
            }
        }
        return defaultValue;
    }

    private String getString(String key, String defaultValue) {
        if( content != null ) {
            JsonNode root = getJsonContent();
            JsonNode node = root.get(key);
            if( node != null && node.isTextual()) {
                return node.textValue();
            }
        }
        return defaultValue;
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
    
}
