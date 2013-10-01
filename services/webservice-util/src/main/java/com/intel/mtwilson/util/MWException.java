package com.intel.mtwilson.util;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.intel.mtwilson.datatypes.AuthResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.i18n.ErrorMessage;
import com.intel.mtwilson.i18n.Message;
import java.util.Locale;
/**
 *
 * @author dsmagadx
 */
public class MWException extends RuntimeException /* extends WebApplicationException */ {
    private final Message message;
//    private AuthResponse authResponse;
    private final ErrorCode code;
    private final Object[] params;
    
    /**
     * Don't allow ourselves to throw an exception without any information at all
     */
    private MWException() {
        this(ErrorCode.UNKNOWN_ERROR);
    }
        
    public MWException(ErrorCode code, Object... params) {
        super(code.name());
        this.message = new ErrorMessage(code, params);
        this.code = code;
        this.params = params;
//        super(Response.status(400).entity(new AuthResponse(code, params)).type(MediaType.APPLICATION_JSON_TYPE).build());
//        authResponse = new AuthResponse(code, params);
    }

    public MWException(Throwable e,ErrorCode code, Object... params) {
        super(code.name(), e);
        this.message = new ErrorMessage(code, params);
        this.code = code;
        this.params = params;
//        super(e,Response.status(400).entity(new AuthResponse(code, params)).type(MediaType.APPLICATION_JSON_TYPE).build());
//        authResponse = new AuthResponse(code, params);
    }

    
    public MWException(ErrorCode code){
        super(code.name());
        this.message = new ErrorMessage(code);
        this.code = code;
        this.params = null;
//        super(Response.status(400).entity(new AuthResponse(code)).type(MediaType.APPLICATION_JSON_TYPE).build());
//        authResponse = new AuthResponse(code);

    }
    
    public MWException(Throwable e){
        super(e);
        this.message = new Message(e.getClass().getCanonicalName());
        this.code = null;
        this.params = null;
//        message = new Message(e.getClass().getCanonicalName()); // XXX do we want to add exception classes to the strings file? for example  java.io.FileNotFoundException=File not found   ... the "getLocalizedMessage()" method of the Throwable class is not really useful, even though it's supposed to be redefined by subclasses, because it assumes the platform default locale and doesn't accept a Locale argument
//        super(e,Response.status(400).entity(new AuthResponse(ErrorCode.SYSTEM_ERROR, e.getMessage())).type(MediaType.APPLICATION_JSON_TYPE).build());
//        authResponse = new AuthResponse(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
        
    public Object[] getParameters() { return params; }
    
    public String getLocalizedMessage(Locale locale) {
        return message.toString(locale);
    }
    
    @Override
    public String getLocalizedMessage() {
        return getLocalizedMessage(Locale.getDefault());
    }
    
    public String getErrorMessage(){
//        return authResponse.getErrorMessage();
        return getLocalizedMessage(); // XXX TODO localized message
    }
    public ErrorCode getErrorCode(){
//        return authResponse.getErrorCodeEnum();
        return code;
    }
}
