/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.intel.mtwilson.i18n.ErrorCode;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 * This object would be used to return back the status of the host registration/update.
 * 
 * @author ssbangal
 */
public class HostConfigResponse {
   
    private String hostName;
    private String status;
    private String errorMessage;
    private ErrorCode errorCode;

    @JsonProperty("Error_Message")
    public String getErrorMessage() {
        return errorMessage;
    }

    @JsonProperty("Error_Message")
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @JsonProperty("Host_Name")
    public String getHostName() {
        return hostName;
    }

    @JsonProperty("Host_Name")
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @JsonProperty("Status")    
    public String getStatus() {
        return status;
    }

    @JsonProperty("Status")        
    public void setStatus(String status) {
        this.status = status;
    }

   @JsonProperty("Error_Code")        
    public ErrorCode getErrorCode() {
        return errorCode;
    }

   @JsonProperty("Error_Code")        
    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
   
}
