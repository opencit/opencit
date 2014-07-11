package com.intel.mtwilson.datatypes;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author ssbangal
 */
public class TxtHostRecordList {
    
    private List<TxtHostRecord> hostRecords = new ArrayList<>();

     @JsonProperty("HostRecords")
    public List<TxtHostRecord> getHostRecords() {
        return hostRecords;
    }

     @JsonProperty("HostRecords")
    public void setHostRecords(List<TxtHostRecord> hostRecords) {
        this.hostRecords = hostRecords;
    }    
    
}
