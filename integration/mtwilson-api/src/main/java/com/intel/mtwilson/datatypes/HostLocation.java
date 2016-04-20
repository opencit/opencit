package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

public class HostLocation {
    
    @JsonProperty("location") public String location;
    @JsonProperty("white_list_value") public String white_list_value;
    
    public HostLocation() {
        
    }
    
    public HostLocation(String location) {
            this.location = location;
            this.white_list_value = "";
    }
    
    public HostLocation(String location, String white_list_value) {
        this.location = location;
        this.white_list_value = white_list_value;
    }

}
