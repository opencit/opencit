/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.i18n.ErrorCode;

/**
 *
 * @author jbuhacoff
 */
public final class ErrorResponse {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ErrorResponse.class);

    //@com.fasterxml.jackson.annotation.JsonProperty("error_code")
    @JsonProperty("error_code")
    private String errorCode;
    
    //@com.fasterxml.jackson.annotation.JsonProperty("error_message")
    @JsonProperty("error_message")
    private String errorMessage;

    
    // this constructor is used by the server when creating the error response
   public ErrorResponse(ErrorCode errorCode, String errorMessage){
//        setErrorCode(message.getErrorCode());
//        setErrorMessage(message);
       this.errorCode = errorCode.name();
       this.errorMessage = errorMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
