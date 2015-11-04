/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.configuration.authentication.status;

import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import com.intel.mtwilson.shiro.setup.HostFilterCheck;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author hmgowda
 */


@V2
@Path("/authentication-status")
public class AuthenticationApi{
    
    @GET
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_PLAIN})
    
    public boolean Authentication_status() throws Exception{
            
    HostFilterCheck auth_status = new HostFilterCheck();
            return auth_status.authentication_check();
    
    
    }
}
