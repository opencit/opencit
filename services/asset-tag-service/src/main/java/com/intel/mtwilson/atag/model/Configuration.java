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

    /**
     * Return value varies by content type.
     * JSON: JsonNode (jackson)
     * XML: JsonNode (jackson)
     * YAML: String
     * PROPERTIES: String
     * @return 
     */
    public Object getContent() {
        try {
            if( ContentType.JSON.equals(contentType)) {
                return json.readTree(content);
            }
            /* disabled because it currently doesn't serialize back to json well... see test.jackson.ConfigurationJsonTest for details
            else if( ContentType.XML.equals(contentType)) {
                return xml.readTree(content);
            }
            */
            /**
             * TODO: XML, YAML, and Java PROPERTIES formats
             * Jackson has a plugin for YAML, so to quickly add support for YAML format, check out https://github.com/FasterXML/jackson-dataformat-yaml
             */
            else if( ContentType.PROPERTIES.equals(contentType) ) {
                Properties p = new Properties();
                p.load(new StringReader(content));
                return p;
            }
            else {
                return content;
            }
        }
        catch(IOException e) {
            log.warn("Cannot deserialize content of type: {}", contentType, e);
            return null;
        }
    }
    
    @JsonIgnore
    public String getContentText() {
        return content;
    }
    
    @JsonIgnore
    public JsonNode getJsonContent() {
        try {
            if( ContentType.JSON.equals(contentType)) {
                return json.readTree(content);
            }
            else {
                return null;
            }
        }
        catch(IOException e) {
            log.warn("Cannot deserialize JSON content", e);
            return null;
        }
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

    public void setContent(Object content) throws JsonProcessingException {
        this.content = json.writeValueAsString(content);
    }
    
    @JsonIgnore
    public void setContentText(String content) {
        this.content = content;
    }

    public void setContentType(ContentType contentType) {
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
