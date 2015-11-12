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
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author hmgowda
 */


@V2
@Path("/authentication-status")
public class AuthenticationApi {

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String authenticationStatus() throws Exception {
        HostFilterCheck authenticationStatus = new HostFilterCheck();
        return authenticationStatus.authentication_check();
    }
}
