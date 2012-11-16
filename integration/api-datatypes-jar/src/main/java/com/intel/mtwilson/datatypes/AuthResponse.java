/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.LoggerFactory;

/**
 *
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
        //this.extraInfo = rootCause.getMessage();
    }
    public AuthResponse(ErrorCode errorCode, Object... extraInfo) {
        this.errorCode = errorCode;
        try{
            this.errorMessage = String.format(errorCode.getMessage(), extraInfo); 
        }catch(Throwable e){
            this.errorMessage = errorCode.getMessage();
            LoggerFactory.getLogger(getClass().getName()).error("Error while formatting error message for " + errorCode.toString() ,e );
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
    
  
    
    @JsonIgnore(true)
    public ErrorCode getErrorCodeEnum(){
        return errorCode;
    }

//    public void setAuthResponse(AuthResponse response) {
//        this.errorCode = response.errorCode;
//    }
}
