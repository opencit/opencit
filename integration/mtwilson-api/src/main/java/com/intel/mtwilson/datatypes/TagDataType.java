package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

public class TagDataType {
    
    @JsonProperty("id") public String id;
    @JsonProperty("uuid") public String uuid;
    @JsonProperty("name") public String name;
    @JsonProperty("oid") public String oid;
    @JsonProperty("values") public String[] values;
    
    public TagDataType() {   
    }
    
    public TagDataType(String id) {
        this.id = id;
        this.uuid = "";
        this.name = "";
        this.oid = "";
        this.values = null;
    }
     public TagDataType(String id, String uuid) {
        this.id = id;
        this.uuid = uuid;
        this.name = "";
        this.oid = "";
        this.values = null;
    }
    
     public TagDataType(String id,String uuid, String name) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.oid = "";
        this.values = null;
    }
     
     public TagDataType(String id,String uuid, String name, String oid) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.oid = oid;
        this.values = null;
    }
     
     public TagDataType(String id,String uuid, String name, String oid, String[] values) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.oid = oid;
        this.values = values;
    }
}
