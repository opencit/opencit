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
/**
 *
 * @author dsmagadx
 */
public class MWException extends WebApplicationException {

    private AuthResponse authResponse;
    private Object[] parameters;
    
    private MWException(){
        
    }
        
    public MWException(ErrorCode code, Object... params) {
        super(Response.status(400).entity(new AuthResponse(code, params)).type(MediaType.APPLICATION_JSON_TYPE).build());
        authResponse = new AuthResponse(code, params);
        parameters = params;
    }

    public MWException(Throwable e,ErrorCode code, Object... params) {
        // bug #1038 suppressing passing the throwable to WebApplicationException because we don't want to print the entire message and possibly stack trace to the UI - those should only go to the log (and whateer has created this exception instance has probably done that already)
        //super(e,Response.status(400).entity(new AuthResponse(code, params)).type(MediaType.APPLICATION_JSON_TYPE).build());
        super(Response.status(400).entity(new AuthResponse(code, params)).type(MediaType.APPLICATION_JSON_TYPE).build());
        authResponse = new AuthResponse(code, params);
        parameters = params;
    }

    
    public MWException(ErrorCode code){
        super(Response.status(400).entity(new AuthResponse(code)).type(MediaType.APPLICATION_JSON_TYPE).build());
        authResponse = new AuthResponse(code);
        parameters = null;
    }
    
    public MWException(Throwable e){
        super(e,Response.status(400).entity(new AuthResponse(ErrorCode.SYSTEM_ERROR, e.getMessage())).type(MediaType.APPLICATION_JSON_TYPE).build());
        authResponse = new AuthResponse(ErrorCode.SYSTEM_ERROR, e.getMessage());
        parameters = null;
    }
    
    public String getErrorMessage(){
        return authResponse.getErrorMessage();
    }
    public ErrorCode getErrorCode(){
        return authResponse.getErrorCodeEnum();
    }

    public Object[] getParameters() {
        return parameters;
    }
    
}
