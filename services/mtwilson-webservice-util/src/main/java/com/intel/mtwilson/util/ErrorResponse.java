/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.util;

import com.intel.dcsg.cpg.i18n.Localizable;
import com.intel.mtwilson.i18n.ErrorMessage;
import com.intel.dcsg.cpg.i18n.Message;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.i18n.ErrorCode;
import java.util.Locale;

/**
 *
 * @author jbuhacoff
 */
public final class ErrorResponse implements Localizable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ErrorResponse.class);

    private Locale locale = null;

    //@com.fasterxml.jackson.annotation.JsonProperty("error_code")
    @JsonProperty("error_code")
    private ErrorCode errorCode;
    
    //@com.fasterxml.jackson.annotation.JsonProperty("error_message")
    @JsonProperty("error_message")
    private Message errorMessage;

    
    // this constructor is used by the server when creating the error response
   public ErrorResponse(ErrorMessage message){
//        setErrorCode(message.getErrorCode());
//        setErrorMessage(message);
       errorCode = message.getErrorCode();
       errorMessage = message;
    }
     /*
    public ErrorResponse(ErrorCode code, Message message) {
        setErrorCode(code); 
        setErrorMessage(message);
    }*/
    
    public String getErrorCode() {
        if( errorCode == null ) {
            log.debug("ErrorResponse getErrorCode called but errorCode is null");
            return null;
        }
        return errorCode.name();
    }

    /*
    public void setErrorCode(ErrorCode value) {
        this.errorCode = value;
    }*/
    
    public String getErrorMessage() {
        if( errorMessage == null ) {
            log.debug("ErrorResponse getErrorMessage called but errorMessage is null");
            return null;
        }
        if( locale == null ) {
            log.debug("ErrorResponse getErrorMessage called but locale is null, using default");
            locale = Locale.getDefault();
        }
        return errorMessage.toString(locale);
    }

    /*
    public void setErrorMessage(Message errorMessage) {
        this.errorMessage = errorMessage;
    }*/

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
