
package com.intel.mtwilson.ms.rest;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.GET;

//import org.codehaus.enunciate.jaxrs.TypeHint;

/**
 * REST Web Service
 * * 
 */

@Stateless
@Path("/status")
public class MsStatus {
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String getServiceStatus() {
        return "MS Service Running 1.2.2 premium ";
    }
}
