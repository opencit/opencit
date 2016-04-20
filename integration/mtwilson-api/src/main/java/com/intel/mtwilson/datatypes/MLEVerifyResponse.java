/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author ssbangal
 */
public class MLEVerifyResponse {
    
    private Boolean biosMLEExists = Boolean.FALSE;
    private Boolean vmmMLEExists = Boolean.FALSE;
    private Boolean errorFlag = Boolean.FALSE;
    private String errorMessage = "";

    @JsonProperty("BIOS_MLE_Exists")        
    public Boolean getBiosMLEExists() {
        return biosMLEExists;
    }

    @JsonProperty("BIOS_MLE_Exists")        
    public void setBiosMLEExists(Boolean biosMLEExists) {
        this.biosMLEExists = biosMLEExists;
    }

    @JsonProperty("VMM_MLE_Exists")        
    public Boolean getVmmMLEExists() {
        return vmmMLEExists;
    }

    @JsonProperty("VMM_MLE_Exists")        
    public void setVmmMLEExists(Boolean vmmMLEExists) {
        this.vmmMLEExists = vmmMLEExists;
    }

    @JsonProperty("Error_Flag")    
    public Boolean getErrorFlag() {
        return errorFlag;
    }

    @JsonProperty("Error_Flag")    
    public void setErrorFlag(Boolean errorFlag) {
        this.errorFlag = errorFlag;
    }

    @JsonProperty("Error_Message")    
    public String getErrorMessage() {
        return errorMessage;
    }

    @JsonProperty("Error_Message")    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    
}
