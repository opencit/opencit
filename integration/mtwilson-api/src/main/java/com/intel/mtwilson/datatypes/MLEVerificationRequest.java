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
public class MLEVerificationRequest {
    private TxtHostRecord hostObj;
    private String biosPCRs;
    private String vmmPCRs;

    public MLEVerificationRequest(TxtHostRecord hostObj, String biosPCRs, String vmmPCRs) {
        this.hostObj = hostObj;
        this.biosPCRs = biosPCRs;
        this.vmmPCRs = vmmPCRs;
    }

    @JsonProperty("HostTXTRecord")
    public TxtHostRecord getHostObj() {
        return hostObj;
    }

    @JsonProperty("HostTXTRecord")    
    public void setHostObj(TxtHostRecord hostObj) {
        this.hostObj = hostObj;
    }

    @JsonProperty("bios_pcrs")
    public String getBiosPCRs() {
        return biosPCRs;
    }

    @JsonProperty("bios_pcrs")
    public void setBiosPCRs(String biosPCRs) {
        this.biosPCRs = biosPCRs;
    }

    @JsonProperty("vmm_pcrs")
    public String getVmmPCRs() {
        return vmmPCRs;
    }

    @JsonProperty("vmm_pcrs")
    public void setVmmPCRs(String vmmPCRs) {
        this.vmmPCRs = vmmPCRs;
    }
     
    
}
