/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

//import com.intel.mtwilson.i18n.ErrorMessage;
//import com.intel.mtwilson.datatypes.ErrorCode;
//import com.intel.mtwilson.util.ErrorResponse;
//import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.shiro.ShiroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
@Provider
//@Component
public class AuthorizationExceptionMapper implements ExceptionMapper<ShiroException> {
    private static Logger log = LoggerFactory.getLogger(AuthorizationExceptionMapper.class);
    
//    @Context private HttpServletRequest request;
    
    @Override
    public Response toResponse(ShiroException e) {
        log.debug("Shiro {}: {}", e.getClass().getName(), e.getMessage(), e);
//        ErrorMessage message = new ErrorMessage(ErrorCode.HTTP_UNAUTHORIZED); // we specifically do not provide any details to the client, to avoid accidentally aiding an attacker; all details are in the server log for the administrator
        
//        Response response = Response.status(Status.UNAUTHORIZED).entity(new ErrorResponse(message)).type(MediaType.APPLICATION_JSON_TYPE).build();
        Response response = Response.status(Status.UNAUTHORIZED).build();
        return response;
    }
    
}
