/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jaxrs;

import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.nio.charset.Charset;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;

/**
 * Utility for the UI to provide users with their "authorization blob"
 * for openstack integrations, etc. which use the http basic authentication.
 * 
 * Note that the client provides the username and password as input to this 
 * utility and we
 * make no attempts to validate their input other than enforcing the
 * rfc-2617 rule that the colon character is not allowed in the username; this utility is purely a 
 * text formatting utility
 * 
 * @author jbuhacoff
 */
@V2
@Path("/util/http-basic-authorization-header-generator")
public class HttpBasicAuthorizationUtil {
    
    public static class BasicAuthorizationInput {
        @FormParam("username")
//        @QueryParam("username")
        public String username;
        @FormParam("password")
//        @QueryParam("password")
        public String password;
    }
    public static class BasicAuthorizationOutput {
        public String authorization;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String generateBasicAuthorizationHeaderText(@BeanParam BasicAuthorizationInput input) {
        if( input.username.contains(":") ) {
            throw new IllegalArgumentException("The colon ':' is not allowed in usernames");
        }
        String credential = String.format("%s:%s", input.username, input.password);
        String encoded  = Base64.encodeBase64String(credential.getBytes(Charset.forName("UTF-8")));
        return String.format("Basic %s", encoded);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML})
    public BasicAuthorizationOutput generateBasicAuthorizationHeader(BasicAuthorizationInput input) {
        BasicAuthorizationOutput output = new BasicAuthorizationOutput();
        output.authorization = generateBasicAuthorizationHeaderText(input);
        return output;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getForm() {
        return "<html><body><form method=\"post\" action=\"http-basic-authorization-header-generator.txt\"><input type=\"text\" name=\"username\"/><input type=\"password\" name=\"password\"/><input type=\"submit\"/></form></body></html>";
    }
    
}
