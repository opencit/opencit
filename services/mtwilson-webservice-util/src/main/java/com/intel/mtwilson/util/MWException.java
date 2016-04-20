package com.intel.mtwilson.util;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


//import com.intel.mtwilson.datatypes.AuthResponse;
import com.intel.dcsg.cpg.i18n.Localizable;
import com.intel.mtwilson.i18n.ErrorMessage;
import javax.ws.rs.WebApplicationException;
import com.intel.mtwilson.i18n.ErrorCode;
import java.util.Locale;
/**
 *
 * @author dsmagadx
 */
public class MWException extends WebApplicationException implements Localizable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MWException.class);

//    private AuthResponse authResponse;
    private ErrorMessage errorMessage;
    private Object[] parameters;
//    private Locale locale;
    
    private MWException(){
        
    }
        
    public MWException(ErrorCode code, Object... params) {
        super(400);
//        super(Response.status(400).entity(new AuthResponse(code, params)).type(MediaType.APPLICATION_JSON_TYPE).build());
//        authResponse = new AuthResponse(code, params);
        errorMessage = new ErrorMessage(code, params);
        parameters = params;
    }

    public MWException(Throwable e,ErrorCode code, Object... params) {
        super(400);
        // bug #1038 suppressing passing the throwable to WebApplicationException because we don't want to print the entire message and possibly stack trace to the UI - those should only go to the log (and whateer has created this exception instance has probably done that already)
        //super(e,Response.status(400).entity(new AuthResponse(code, params)).type(MediaType.APPLICATION_JSON_TYPE).build());
//        super(Response.status(400).entity(new AuthResponse(code, params)).type(MediaType.APPLICATION_JSON_TYPE).build());
//        authResponse = new AuthResponse(code, params);
        errorMessage = new ErrorMessage(code, params);
        parameters = params;
    }

    
    public MWException(ErrorCode code){
        super(400);
//        super(Response.status(400).entity(new AuthResponse(code)).type(MediaType.APPLICATION_JSON_TYPE).build());
//        authResponse = new AuthResponse(code);
        errorMessage = new ErrorMessage(code);
        parameters = null;
    }
    
    public MWException(Throwable e){
//        super(e,Response.status(400).entity(new AuthResponse(ErrorCode.SYSTEM_ERROR, e.getMessage())).type(MediaType.APPLICATION_JSON_TYPE).build());
//        authResponse = new AuthResponse(ErrorCode.SYSTEM_ERROR, e.getMessage());
        super(400);
        errorMessage = new ErrorMessage(ErrorCode.SYSTEM_ERROR, e.getMessage()); 
        parameters = null;
    }
    
    public String getErrorMessage(){
        log.debug("MWException getErrorMesage");
//        return authResponse.getErrorMessage();
        return errorMessage.toString();
    }
    public ErrorCode getErrorCode(){
        log.debug("MWException getErrorCode");
//        return authResponse.getErrorCodeEnum();
        return errorMessage.getErrorCode();
    }

    public Object[] getParameters() {
        log.debug("MWException getParameters");
        return parameters;
    }

    @Override
    public void setLocale(Locale locale) {
        log.debug("MWException setLocale");
        errorMessage.setLocale(locale);
//        this.locale = locale;
    }

    @Override
    public String toString() {
        log.debug("MWException toString: {}", errorMessage.toString());
        return errorMessage.toString();
    }
    
    
}
