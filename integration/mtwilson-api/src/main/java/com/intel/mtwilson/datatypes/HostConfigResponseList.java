/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author ssbangal
 */
public class HostConfigResponseList {
    
    private List<HostConfigResponse> hostRecords = new ArrayList<HostConfigResponse>();

    @JsonProperty("HostRecords")
    public List<HostConfigResponse> getHostRecords() {
        return hostRecords;
    }

    @JsonProperty("HostRecords")
    public void setHostRecords(List<HostConfigResponse> hostRecords) {
        this.hostRecords = hostRecords;
    }    
    
}
