/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.intel.mtwilson.i18n.ErrorCode;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class HostTrust extends AuthResponse{

    private String hostName;
    private Integer vmmStatus;
    private Integer biosStatus;
    
    

    public HostTrust(ErrorCode errorCode, String errorMessage) {
        super(errorCode,new Object[]{errorMessage});
    }
    
    private HostTrust(){
        
    }
    
    

    public HostTrust(String hostName, Integer vmmStatus, Integer biosStatus) {
        super(ErrorCode.OK);
        setIpAddress(hostName);
        setVmmStatus(vmmStatus);
        setBiosStatus(biosStatus);
    }
    
    public HostTrust(ErrorCode errorCode,String errorMessage,String hostName, Integer vmmStatus, Integer biosStatus  ){
        super(errorCode,(errorMessage != null)?new Object[]{errorMessage}:null);
        setIpAddress(hostName);
        setVmmStatus(vmmStatus);
        setBiosStatus(biosStatus);
    }

    @JsonProperty("bios_status")
    public Integer getBiosStatus() {
        return biosStatus;
    }

    @JsonProperty("bios_status")
    public void setBiosStatus(Integer biosStatus) {
        this.biosStatus = biosStatus;
    }

    @JsonProperty("host_name")
    public String getIpAddress() {
        return hostName;
    }

    @JsonProperty("host_name")
    public void setIpAddress(String hostName) {
        this.hostName = hostName;
    }

    @JsonProperty("vmm_status")
    public Integer getVmmStatus() {
        return vmmStatus;
    }

    @JsonProperty("vmm_status")
    public void setVmmStatus(Integer vmmStatus) {
        this.vmmStatus = vmmStatus;
    }
}
