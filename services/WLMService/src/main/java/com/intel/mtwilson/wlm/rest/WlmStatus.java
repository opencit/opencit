package com.intel.mtwilson.wlm.rest;


import javax.ejb.Stateless;
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

@Stateless
@Path("/status")
public class WlmStatus {
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String getServiceStatus() {
        return "WLM Service Running 1.2.2 premium ";
    }
}
