/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.wlm.rest.data;

import com.intel.mountwilson.as.common.ValidationException;
import org.codehaus.jackson.annotate.JsonGetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

/**
 *
 * @author dsmagadx
 */
public final class OsData {


    private String name;
    private String version;
    private String description;

    public OsData(){
        
    }
    
    public OsData(String name, String version, String description) {
        setName(name); 
        setVersion(version);
        setDescription(description);
    }
    
    
    

    @JsonGetter("Description")
    public String getDescription() {

        return description;
    }

    @JsonSetter("Description")
    public void setDescription(String value) {


        this.description = value;
    }

    @JsonGetter("Name")
    public String getName() {
        if (name == null || name.isEmpty()) {
            throw new ValidationException("OS Name is missing");
        }

        return this.name;
    }

    @JsonSetter("Name")
    public void setName(String value) {
        this.name = value;

    }

    @JsonGetter("Version")
    public String getVersion() {
        if (version == null || version.isEmpty()) {
            throw new ValidationException("OS Version is missing");
        }
        return version;
    }

    @JsonSetter("Version")
    public void setVersion(String value) {
        this.version = value;
    }
}
