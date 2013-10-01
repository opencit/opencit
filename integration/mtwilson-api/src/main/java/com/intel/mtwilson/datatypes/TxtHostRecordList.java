/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author ssbangal
 */
public class TxtHostRecordList {
    
    private List<TxtHostRecord> hostRecords = new ArrayList<TxtHostRecord>();

     @JsonProperty("HostRecords")
    public List<TxtHostRecord> getHostRecords() {
        return hostRecords;
    }

     @JsonProperty("HostRecords")
    public void setHostRecords(List<TxtHostRecord> hostRecords) {
        this.hostRecords = hostRecords;
    }    
    
}
