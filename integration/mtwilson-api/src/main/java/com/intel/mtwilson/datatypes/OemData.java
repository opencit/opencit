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
public final class OemData extends ObjectModel {


    private String name = null;
    private String description  = null;

    public OemData(){
        
    }
    
    public OemData(String name) {
        setName(name); 
    }
    public OemData(String name, String description) {
        setName(name); 
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

    @Override
    protected void validate() {
        if (name == null || name.isEmpty()) {
            fault("OEM Name is missing");
        }
        
    }
}
