/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
//import org.codehaus.jackson.annotate.JsonGetter;
//import org.codehaus.jackson.annotate.JsonProperty;
//import org.codehaus.jackson.annotate.JsonSetter;
import com.intel.dcsg.cpg.validation.ObjectModel;

/**
 *
 * @author dsmagadx
 */
public final class OsData extends ObjectModel {


    private String name = null;
    private String version = null;
    private String description = null;

    public OsData(){
        
    }
 
    public OsData(String name, String version) {
        setName(name); 
        setVersion(version);
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

        return this.name;
    }

    @JsonSetter("Name")
    public void setName(String value) {
        this.name = value;

    }

    @JsonGetter("Version")
    public String getVersion() {
        return version;
    }

    @JsonSetter("Version")
    public void setVersion(String value) {
        this.version = value;
    }
    
    @Override
    protected void validate() {
        if (version == null || version.isEmpty()) {
            fault("OS Version is missing");
        }
        if (name == null || name.isEmpty()) {
            fault("OS Name is missing");
        }
        
    }
}
