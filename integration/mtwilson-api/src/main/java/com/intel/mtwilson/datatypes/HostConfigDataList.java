package com.intel.mtwilson.datatypes;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author ssbangal
 */
public class HostConfigDataList {
    private List<HostConfigData> hostRecords = new ArrayList<>();

   @JsonProperty("HostRecords")
    public List<HostConfigData> getHostRecords() {
        return hostRecords;
    }

   @JsonProperty("HostRecords")
    public void setHostRecords(List<HostConfigData> hostRecords) {
        this.hostRecords = hostRecords;
    }
    
    
}
