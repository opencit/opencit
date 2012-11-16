/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.ms.business.HostBO;
import com.intel.mtwilson.ms.helper.MSComponentFactory;
import com.intel.mtwilson.ApiException;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.HostConfigData;
//import javax.annotation.security.RolesAllowed;
import com.intel.mtwilson.security.annotations.*;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author ssbangal
 */
@Stateless
@Path("/host")
public class Host {

    @Context
    private UriInfo context;
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    /**
     * Constructor must be public for framework to instantiate this REST API.
     */
    public Host() {
    }

    /**
     * Retrieves representation of an instance of com.intel.mountwilson.ms.rest.Host
     * @return an instance of java.lang.String
     */
    @RolesAllowed({"Attestation"})
    @GET
    @Produces("application/json")
    public String getJson() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of Host
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @RolesAllowed({"Attestation"})
    @PUT
    @Consumes("application/json")
    public void putJson(String content) {
    }
    
    @RolesAllowed({"Attestation"})
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String registerHost(TxtHostRecord hostObj) throws ApiException {
        boolean result = new MSComponentFactory().getHostBO().registerHost(hostObj);
        return Boolean.toString(result);
    }
    
    @RolesAllowed({"Attestation"})
    @POST
    @Path("/custom")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String registerHost(HostConfigData hostConfigObj) throws ApiException {
        boolean result = new MSComponentFactory().getHostBO().registerHostFromCustomData(hostConfigObj);
        return Boolean.toString(result);
    }

    @RolesAllowed({"Whitelist"})
    @POST
    @Path("/whitelist")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String configureWhiteList(TxtHostRecord hostObj) throws ApiException {
        boolean result = new MSComponentFactory().getHostBO().configureWhiteListFromHost(hostObj);
        return Boolean.toString(result);
    }
    
    @RolesAllowed({"Whitelist"})
    @POST
    @Path("/whitelist/custom")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String configureWhiteList(HostConfigData hostConfigObj) throws ApiException {
        boolean result = new MSComponentFactory().getHostBO().configureWhiteListFromCustomData(hostConfigObj);
        return Boolean.toString(result);
    }
}
