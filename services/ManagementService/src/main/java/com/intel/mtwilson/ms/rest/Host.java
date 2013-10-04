/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.ms.business.HostBO;
import com.intel.mtwilson.ms.helper.MSComponentFactory;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.ms.common.MSException;
//import javax.annotation.security.RolesAllowed;
import com.intel.mtwilson.security.annotations.*;
import java.util.ArrayList;
import java.util.List;
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
   
   

    /**
     * This method also supports the registration/update of multiple hosts with a single call. In this case the user has passed in additional details regarding
     * the white list targets that should be used.
     * 
     * @param hostRecords
     * @return: List of HostConfigResponse objects each one having the status of the registration or update of the host passed in.
     * @throws ApiException 
     */
   @RolesAllowed({"Attestation", "Security"})
    @POST
    @Path("/bulk/custom")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HostConfigResponseList registerHosts(HostConfigDataList hostRecords) throws ApiException {
          HostConfigResponseList results = new MSComponentFactory().getHostBO().registerHosts(hostRecords);
          return results;
        /*HostConfigResponseList hostResponses = new HostConfigResponseList();
        List <HostConfigResponse> hostResList = new ArrayList<HostConfigResponse>();
        // Process all the hosts one by one.
        if (hostRecords != null && !hostRecords.getHostRecords().isEmpty()) {
                for (HostConfigData hostRecord: hostRecords.getHostRecords()) {
                        HostConfigResponse hostResponse = new HostConfigResponse();
                        hostResponse.setHostName(hostRecord.getTxtHostRecord().HostName);
                        // Since we do not want to throw exception for each host separately, we capture the exception details into the error message field, which would be sent back to the caller.
                        try {
                                boolean result = new MSComponentFactory().getHostBO().registerHostFromCustomData(hostRecord);
                                hostResponse.setStatus(Boolean.toString(result));
                                hostResponse.setErrorMessage("");
                        } catch (MSException mse) {
                                hostResponse.setStatus(Boolean.toString(false));
                                hostResponse.setErrorMessage(mse.getErrorMessage()+ "[" + mse.getErrorCode() + "]");
                        }
                        hostResList.add(hostResponse);
                }           
       }
       hostResponses.setHostRecords(hostResList);
        return hostResponses;*/
    }
    /**
     * This new method supports registration/update of multiple hosts with a single call. 
     * Since the user has passed in the plain TxtHostRecord object we will use the default white list targets for both BIOS and VMM and register/update the host.
     * 
     * @param hostRecords
     * @return : List of HostConfigResponse objects each one having the status of the registration or update of the host passed in.
     * @throws ApiException 
    */
    @RolesAllowed({"Attestation", "Security"})
    @POST
    @Path("/bulk")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HostConfigResponseList registerHosts(TxtHostRecordList hostRecords) throws ApiException {
        log.warn("About to execute the registerhosts function");
          HostConfigResponseList results = new MSComponentFactory().getHostBO().registerHosts(hostRecords);
          return results;        
       /*HostConfigResponseList hostResponses = new HostConfigResponseList();
        List <HostConfigResponse> hostResList = new ArrayList<HostConfigResponse>();       
       if (hostRecords != null && !hostRecords.getHostRecords().isEmpty()) {
             for (TxtHostRecord hostRecord: hostRecords.getHostRecords()) {
                HostConfigResponse hostResponse = new HostConfigResponse();
                hostResponse.setHostName(hostRecord.HostName);
                hostResponse.setStatus(Boolean.toString(false));
                 hostResponse.setErrorMessage("Test");
                log.error("Processed host {} successfully",hostRecord.HostName );
                hostResList.add(hostResponse);
            }           
       }
       // boolean result = new MSComponentFactory().getHostBO().registerHost(hostObj);
       hostResponses.setHostRecords(hostResList);
        return hostResponses;*/
    }
    
   

}
