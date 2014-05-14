/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Document;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="configuration")
public class Configuration extends Document{
    
    private String name;
    private Properties content;
    private static final ObjectMapper json = new ObjectMapper();
    private static final XmlMapper xml = new XmlMapper(); 
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
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
