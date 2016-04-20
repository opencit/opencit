
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.launcher.ws.ext.V1;
//import javax.ejb.Stateless;
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
@V1
//@Stateless
@Path("/ManagementService/resources/msstatus")
public class MsStatus {
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String getServiceStatus() {
        return "MS Service Running " + getClass().getPackage().getImplementationVersion() + " premium ";
    }
}
