/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.intel.dcsg.cpg.i18n.Localizable;
import com.intel.mtwilson.i18n.ErrorMessage;
import com.intel.mtwilson.i18n.Message;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    /*
    public ErrorResponse(){
        
    }*/
 
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
        return errorCode.name();
    }

    /*
    public void setErrorCode(ErrorCode value) {
        this.errorCode = value;
    }*/
    
    public String getErrorMessage() {
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
