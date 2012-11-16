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
    
    private MWException(){
        
    }
        
    public MWException(ErrorCode code, Object... params) {
        super(Response.status(400).entity(new AuthResponse(code, params)).type(MediaType.APPLICATION_JSON_TYPE).build());
        authResponse = new AuthResponse(code, params);
    }

    public MWException(Exception e,ErrorCode code, Object... params) {
        super(e,Response.status(400).entity(new AuthResponse(code, params)).type(MediaType.APPLICATION_JSON_TYPE).build());
        authResponse = new AuthResponse(code, params);
    }

    
    public MWException(ErrorCode code){
        super(Response.status(400).entity(new AuthResponse(code)).type(MediaType.APPLICATION_JSON_TYPE).build());
        authResponse = new AuthResponse(code);

    }
    
    public MWException(Exception e){
        super(e,Response.status(400).entity(new AuthResponse(ErrorCode.SYSTEM_ERROR, e.getMessage())).type(MediaType.APPLICATION_JSON_TYPE).build());
        authResponse = new AuthResponse(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
    
    public String getErrorMessage(){
        return authResponse.getErrorMessage();
    }
    public ErrorCode getErrorCode(){
        return authResponse.getErrorCodeEnum();
    }
}
