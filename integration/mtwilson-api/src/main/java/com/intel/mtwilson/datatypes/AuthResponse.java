/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;


import com.intel.mtwilson.i18n.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.LoggerFactory;

/**
 * @author dsmagadx
 */
public class AuthResponse {

    private ErrorCode errorCode = ErrorCode.OK;
    private String errorMessage = null;
   

    public AuthResponse() {
        this.errorCode = ErrorCode.OK;
        this.errorMessage = ErrorCode.OK.getMessage();
    }

    public AuthResponse(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getMessage();
    }

//    public AuthResponse(ErrorCode errorCode, String extraInfo) {
//        this.errorCode = errorCode;
//        this.errorMessage = String.format(errorCode.getMessage(), extraInfo);
//    }
    public AuthResponse(ErrorCode errorCode, String errorMessage, Throwable rootCause) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        //this.extraInfo = rootCause.getMessage(); // bug #1038 if we do provide any info from rootCause it must only be rootCause.getClass().getSimpleName() -  the Message can only go in the error log
    }
    public AuthResponse(ErrorCode errorCode, Object... extraInfo) {
        this.errorCode = errorCode;
        try{
            this.errorMessage = String.format(errorCode.getMessage(), extraInfo); 
        }catch(Throwable e){
            this.errorMessage = errorCode.name(); // bug #1038 if we have an error formatting the message then only print the error code enum name like SYSTEM_ERROR  errorCode.getMessage();
            LoggerFactory.getLogger(getClass().getName()).error("Error while formatting error message for " + errorCode.name() ,e );
        }   
    }
    
    public AuthResponse(AuthResponse response) {
        this.errorMessage = response.getErrorMessage();
        this.errorCode = response.getErrorCodeEnum();
    }

    @JsonProperty("error_code")
    public String getErrorCode() {
        return errorCode.toString(); // so we see "VALIDATION_ERROR" instead of "1006"
    }

    @JsonProperty("error_message")
    public String getErrorMessage() {
        return errorMessage;
    }

    @JsonProperty("error_code")
    public void setErrorCode(String errorCode) {
        this.errorCode = ErrorCode.valueOf(errorCode);
    }

    @JsonProperty("error_message")
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
  
    
    //@org.codehaus.jackson.annotate.JsonIgnore(true) // jackson 1.9
    @JsonIgnore // jackson 2.0
    public ErrorCode getErrorCodeEnum(){
        return errorCode;
    }

//    public void setAuthResponse(AuthResponse response) {
//        this.errorCode = response.errorCode;
//    }
}
