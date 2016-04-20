/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.wlm.rest;

import com.intel.mtwilson.wlm.business.OsBO;
import com.intel.mtwilson.datatypes.OsData;
import java.util.List;
//import javax.annotation.security.RolesAllowed;
import com.intel.mtwilson.security.annotations.*;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.launcher.ws.ext.V1;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * REST Web Service
 *
 * @author dsmagadx
 */
@V1
@Path("/WLMService/resources/os")
public class Os {

    
    private OsBO osBO;

    /** Creates a new instance of Os */
    public Os() {
        osBO = new OsBO();
    }

    /**
     * Retrieves representation of list of all instances of com.intel.mountwilson.wlm.rest.Os
     * in the database
     * @return an instance of 
     */
    @GET
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions("oss:search,retrieve")
    @Produces("application/json")
    public List<OsData> listAllOs() {
        return osBO.getAllOs();
    }

    /**
     * Updates the specified OS in the database. If it can be updated a success message
     * is returned. If not, an error message is returned.
     * Sample request:
     * PUT http://localhost:8080/WLMService/resources/os
     * {"Name":"OS Name ","Version":"Os Version","Description":"Os Description"}
     * Sample success output:
     * "true"
     * Sample error output:
     * { "error_message":"Unknown error - Error while creating MLE in WLM Service", "error_code":1002 }
     * 
     * @param osData record as described
     * @return 
     */
    @PUT
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions("oss:store")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String updateOs(OsData osData) {
        ValidationUtil.validate(osData);
        return osBO.updateOs(osData, null);
    }
    /**
     * Adds the specified OS in the database. If it can be added a success message
     * is returned. If not, an error message is returned.
     * Sample request:
     * PUT http://localhost:8080/WLMService/resources/os
     * {"Name":"OS Name ","Version":"Os Version","Description":"Os Description"}
     * Sample success output:
     * "true"
     * Sample error output:
     * { "error_message":"Unknown error - Error while creating MLE in WLM Service", "error_code":1002 }
     * 
     * @param osData record as described
     * @return 
     */
    @POST
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions("oss:create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addOs(OsData osData) {
        ValidationUtil.validate(osData);
        return osBO.createOs(osData, null);
    }
    
    
    @DELETE
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions("oss:delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteOs(@QueryParam("Name")String osName, @QueryParam("Version")String osVersion ) {
        ValidationUtil.validate(osName);
        ValidationUtil.validate(osVersion);
        return osBO.deleteOs(osName,osVersion, null);
    }
    
    

}
