package com.intel.mtwilson.wlm.rest;


import com.intel.mtwilson.launcher.ws.ext.V1;
//import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.GET;

/**
 * REST Web Service
 * * 
 */

@V1
//@Stateless
@Path("/WLMService/resources/wlmstatus")
public class WlmStatus {
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String getServiceStatus() {
        return "WLM Service Running " + getClass().getPackage().getImplementationVersion() + " premium ";
    }
}
