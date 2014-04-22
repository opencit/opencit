/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.test.shiro;

import com.intel.dcsg.cpg.performance.Task;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityTestResource.class);
    
    
    @GET
    @Path("/default")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloDefault() {
        return "hello! this method does not have any security annotations on it";
    }
    
    // an authenticated user will not be allowed to access this method ; they would have to logout first to access it
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

    @RequiresPermissions("test:hello,goodbye")
    @GET
    @Path("/permission2")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloPermittedUser2() {
        return "hello, permitted user! you have 'hello,goodbye' access to the 'test' resource";
    }

    @RequiresPermissions({"test:hello","test:goodbye"})
    @GET
    @Path("/permission3")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloPermittedUser3() {
        return "hello, permitted user! you have 'hello' and 'goodbye' access to the 'test' resource";
    }
    
    
    @RequiresRoles("test")
    @GET
    @Path("/rolebased1")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloRoleBasedUser1() {
        return "hello, role-based user! you have the 'test' role";
    }
    @RequiresRoles("root")
    @GET
    @Path("/rolebased2")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloRoleBasedUser2() {
        return "hello, role-based user! you have the 'root' role";
    }

    @RequiresRoles({"root","test"}) // requires ALL listed roles
    @GET
    @Path("/rolebased3")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloRoleBasedUser3() {
        return "hello, role-based user! you have both the 'root' and the 'test' roles";
    }
    
    @RequiresRoles(value={"root","test"}, logical=Logical.OR) // requires AT LEAST ONE 
    @GET
    @Path("/rolebased4")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloRoleBasedUser4() {
        return "hello, role-based user! you have the 'root' or the 'test' role or both";
    }
    
    @RequiresUser
    @GET
    @Path("/user")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloRememberMeUser() {
        Subject currentUser = SecurityUtils.getSubject();
        return "hello, user! authenticated="+String.valueOf(currentUser.isAuthenticated())+" but we remember you "+currentUser.getPrincipal().getClass().getName();
    }
    
    
    @RequiresPermissions({"test:thread"})
    @GET
    @Path("/permission-thread-executor")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloPermittedUserNewTaskThread() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService scheduler = Executors.newFixedThreadPool(1);
        SubTask task = new SubTask();
        Future<?> status = scheduler.submit(task);
        status.get(5, TimeUnit.SECONDS); // 5 seconds timeout        ; throws InterruptedException, ExecutionException, TimeoutException
        return String.format("hello, permitted user! you have 'thread' access to the 'test' resource and the result is: %s",task.isDone());
    }
    
    public static class SubTask extends Task {

        @RequiresRoles("unobtanium")
        @RequiresPermissions({"test:thread"})
        @Override
        protected void execute() throws Exception {
            log.debug("executing subtask");
            throw new Exception("exception from subtask");
        }
        
    }
    
}
