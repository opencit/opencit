/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class PcrLogReport {
    private Integer trustStatus;
    private Integer name;
    private String value;

    private Date verifiedOn;
    private String whiteListValue;
    
    
    
    private List<ModuleLogReport> moduleLogs = new ArrayList<ModuleLogReport>();
    
    
    @JsonProperty("Name")
    public Integer getName() {
        return name;
    }

    @JsonProperty("Name")
    public void setName(Integer name) {
        this.name = name;
    }

    @JsonProperty("TrustStatus")
    public Integer getTrustStatus() {
        return trustStatus;
    }

    @JsonProperty("TrustStatus")
    public void setTrustStatus(Integer trustStatus) {
        this.trustStatus = trustStatus;
    }

    @JsonProperty("Value")
    public String getValue() {
        return value;
    }

    @JsonProperty("Value")
    public void setValue(String value) {
        this.value = value;
    }

    @JsonProperty("Verified_On")
    public Date getVerifiedOn() {
        return verifiedOn;
    }

    @JsonProperty("Verified_On")
    public void setVerifiedOn(Date verifiedOn) {
        this.verifiedOn = verifiedOn;
    }

    @JsonProperty("ModuleLogs")
    public void setModuleLogs(List<ModuleLogReport> moduleLogs) {
        this.moduleLogs = moduleLogs;
    }


    @JsonProperty("WhitelistValue")
    public String getWhiteListValue() {
        return whiteListValue;
    }
    
    @JsonProperty("WhitelistValue")
    public void setWhiteListValue(String whiteListValue) {
        this.whiteListValue = whiteListValue;
    }
    
    @JsonProperty("ModuleLogs")
    public List<ModuleLogReport> getModuleLogs() {
        return moduleLogs;
    }

}
