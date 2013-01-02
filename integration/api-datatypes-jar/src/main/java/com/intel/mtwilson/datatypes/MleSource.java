/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import org.codehaus.jackson.annotate.JsonProperty;
/**
 *
 * @author ssbangal
 */
public class MleSource {
    
    private MleData mleData;
    private String hostName;

    @JsonProperty("HostName")
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @JsonProperty("MleData")
    public MleData getMleData() {
        return mleData;
    }

    public void setMleData(MleData mleData) {
        this.mleData = mleData;
    } 
    
}
