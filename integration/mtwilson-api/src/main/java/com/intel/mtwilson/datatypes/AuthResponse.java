/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;


import com.intel.mtwilson.i18n.ErrorMessage;
import com.intel.dcsg.cpg.i18n.Localizable;
import java.util.Locale;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author dsmagadx
 */
public class AuthResponse implements Localizable {
    private static Logger log = LoggerFactory.getLogger(AuthResponse.class);
    
    private ErrorMessage errorMessage;
    private ErrorCode errorCode = ErrorCode.OK;
    private String jsonErrorMessage = null; // only set when deserializing a server response... 
//    private Object[] args = null;
    private Locale locale = null;
   
    public AuthResponse() {
        this(ErrorCode.OK);
//        this.errorCode = ErrorCode.OK;
//        this.args = null;
//        this.errorMessage = ErrorCode.OK.getMessage();
    }
    
    public AuthResponse(ErrorMessage errorMessage) {
        this.errorCode = errorMessage.getErrorCode();
//        this.args = errorMessage.getParameters();
        this.errorMessage = errorMessage;
    }

    public AuthResponse(ErrorCode errorCode) {
        this.errorCode = errorCode;
//        this.args = null;
        this.errorMessage = new ErrorMessage(errorCode);
//        this.errorMessage = errorCode.getMessage();
    }

//    public AuthResponse(ErrorCode errorCode, String extraInfo) {
//        this.errorCode = errorCode;
//        this.errorMessage = String.format(errorCode.getMessage(), extraInfo);
//    }
    /*
    public AuthResponse(ErrorCode errorCode, String errorMessage, Throwable rootCause) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
//        this.extraInfo = rootCause.getMessage();
    }*/
    public AuthResponse(ErrorCode errorCode, Object... extraInfo) {
        this.errorCode = errorCode;
//        this.args = extraInfo;
        this.errorMessage = new ErrorMessage(errorCode, extraInfo);
        /*
        try{
//            this.errorMessage = String.format(errorCode.getMessage(), extraInfo); 
        }catch(Throwable e){
//            this.errorMessage = errorCode.getMessage();
            LoggerFactory.getLogger(getClass().getName()).error("Error while formatting error message for " + errorCode.toString() ,e );
        }   
        */
    }

    /*
    public AuthResponse(AuthResponse response) {
//        this.errorMessage = response.getErrorMessage();
        this.errorCode = response.getErrorCodeEnum();
    }
*/
    @JsonProperty("error_code")
    public String getErrorCode() {
        return errorCode.name(); // so we see "VALIDATION_ERROR" instead of "1006"
    }

    @JsonProperty("error_message")
    public String getErrorMessage() {
        if( jsonErrorMessage != null ) {
            log.debug("AuthResponse already has JSON message: {}", jsonErrorMessage);
            return jsonErrorMessage; // already localized;  this is the case when the client deserializes json replies from the server (jackson calls setErrorMessage)
        }
        if( locale != null ) {
            log.debug("AuthResponse locale has been set: {}", locale.toString());
            log.debug("AuthResponse using custom locale: {}", errorMessage.toString());
            return errorMessage.toString(locale);
        }
        else {
            log.debug("AuthResponse using default locale: {}", errorMessage.toString());
            return errorMessage.toString();
        }
//        return errorMessage;
    }

    @JsonProperty("error_code")
    public void setErrorCode(String errorCode) {
        this.errorCode = ErrorCode.valueOf(errorCode);
    }

    @JsonProperty("error_message")
    public void setErrorMessage(String errorMessage) {
        this.jsonErrorMessage = errorMessage;
    }
    
  
    
    @JsonIgnore(true)
    public ErrorCode getErrorCodeEnum(){
        return errorCode;
    }

//    public void setAuthResponse(AuthResponse response) {
//        this.errorCode = response.errorCode;
//    }
    
    @JsonIgnore
    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
