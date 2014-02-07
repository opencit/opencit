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
public class HostConfigDataList {
    
    private List<HostConfigData> hostRecords = new ArrayList<HostConfigData>();

   @JsonProperty("HostRecords")
    public List<HostConfigData> getHostRecords() {
        return hostRecords;
    }

   @JsonProperty("HostRecords")
    public void setHostRecords(List<HostConfigData> hostRecords) {
        this.hostRecords = hostRecords;
    }
    
    
}
