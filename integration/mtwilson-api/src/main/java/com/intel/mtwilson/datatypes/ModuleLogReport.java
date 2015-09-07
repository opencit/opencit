/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class ModuleLogReport {
    private Integer trustStatus;
    private String componentName;
    private String value;
    private String whitelistValue;
    private List<ModuleLogReport> moduleLogs = new ArrayList<ModuleLogReport>();
    
    public ModuleLogReport(){
        
    }

    public ModuleLogReport(String componentName, String value, String whitelistValue, Integer trustStatus) {
        this.componentName = componentName;
        this.value = value;
        this.whitelistValue = whitelistValue;
        this.trustStatus = trustStatus;
    }
    
    
    @JsonProperty("ComponentName")
    public String getComponentName() {
        return componentName;
    }

    @JsonProperty("ComponentName")
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    @JsonProperty("Value")
    public String getValue() {
        return value;
    }

    @JsonProperty("Value")
    public void setValue(String value) {
        this.value = value;
    }
    @JsonProperty("WhitelistValue")
    public String getWhitelistValue() {
        return whitelistValue;
    }

    @JsonProperty("WhitelistValue")
    public void setWhitelistValue(String whitelistValue) {
        this.whitelistValue = whitelistValue;
    }
    
    
      @JsonProperty("TrustStatus")
    public Integer getTrustStatus() {
        return trustStatus;
    }

    @JsonProperty("TrustStatus")
    public void setTrustStatus(Integer trustStatus) {
        this.trustStatus = trustStatus;
    }
    
    @JsonProperty("ModuleLogs")
    public void setModuleLogs(List<ModuleLogReport> moduleLogs) {
        this.moduleLogs = moduleLogs;
    }

    @JsonProperty("ModuleLogs")
    public List<ModuleLogReport> getModuleLogs() {
        return moduleLogs;
    }
    
}
