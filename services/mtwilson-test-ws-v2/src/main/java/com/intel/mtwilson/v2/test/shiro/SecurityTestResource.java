/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.test.shiro;

import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/test/security")
public class SecurityTestResource {
    
    @RequiresGuest
    @GET
    @Path("/guest")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloGuest() {
        return "hello, guest!";
    }


    @RequiresAuthentication
    @GET
    @Path("/authenticated")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloAuthenticatedUser() {
        Subject currentUser = SecurityUtils.getSubject();
        return "hello, authenticated user! "+currentUser.getPrincipal().getClass().getName();
    }

    @RequiresPermissions("test:hello")
    @GET
    @Path("/permission")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloPermittedUser() {
        return "hello, permitted user! you have 'hello' access to the 'test' resource";
    }

    @RequiresRoles("test")
    @GET
    @Path("/rolebased")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloRoleBasedUser() {
        return "hello, role-based user! you have the 'test' role";
    }

    @RequiresUser
    @GET
    @Path("/user")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloRememberMeUser() {
        Subject currentUser = SecurityUtils.getSubject();
        return "hello, user! you are not authenticated but we remember you "+currentUser.getPrincipal().getClass().getName();
    }
    
}
