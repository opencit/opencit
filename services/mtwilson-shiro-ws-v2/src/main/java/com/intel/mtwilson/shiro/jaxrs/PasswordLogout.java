/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jaxrs;

import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.security.GeneralSecurityException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Invalidates an authorization token immediately. 
 * 
 * @author jbuhacoff
 */
@V2
@Path("/logout")
public class PasswordLogout {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordLogout.class);
    
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
//    public void submitLoginForm(@Context final HttpServletRequest request, @Context final HttpServletResponse response, @FormParam("username") String username, @FormParam("password") String password) {
    public void submitLogoutForm(@Context final HttpServletRequest request, @Context final HttpServletResponse response, @BeanParam PasswordLogoutRequest passwordLogoutRequest) throws GeneralSecurityException {
//        log.debug("submitLoginForm username {} password {}", username, password);
        log.debug("submitLoginForm beanparam token {}", passwordLogoutRequest.getAuthorizationToken());
        log.debug("request from {}", request.getRemoteHost());

//        logoutRequest(request, response, passwordLogoutRequest);
        log.debug("Successfully processed logout request with auth token {}.", passwordLogoutRequest.getAuthorizationToken());
        
        response.setStatus(204);
    }
    
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public void logoutRequest(@Context final HttpServletRequest request, @Context final HttpServletResponse response, PasswordLogoutRequest passwordLogoutRequest) throws GeneralSecurityException {
        log.debug("logoutRequest token {}", passwordLogoutRequest.getAuthorizationToken());
        log.debug("request from {}", request.getRemoteHost());
        response.setStatus(204);
    }    
    
}
