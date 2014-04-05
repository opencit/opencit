/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.wlm.rest;

import com.intel.mtwilson.wlm.business.OemBO;
import com.intel.mtwilson.datatypes.OemData;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author dsmagadx
 */
@V1
@Path("/WLMService/resources/oem")
public class Oem {
    private Logger log = LoggerFactory.getLogger(getClass());
    private OemBO oemBo;

    /** Creates a new instance of Oem */
    public Oem() {
        oemBo = new OemBO();
    }

    /**
     * Retrieves representation of list of all instances of com.intel.mountwilson.wlm.rest.OEM
     * in the database
     * @return an instance of 
     */
    @GET
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions("oems:search")        
    @Produces("application/json")
    public List<OemData> listAllOem() {
        return oemBo.getAllOem();
    }

    /**
     * Updates the specified OME in the database. If it can be updated a success message
     * is returned. If not, an error message is returned.
     * Sample request:
     * PUT http://localhost:8080/WLMService/resources/oem
     * {"Name":"OME Name ","Version":"Os Version","Description":"Os Description"}
     * Sample success output:
     * "true"
     * Sample error output:
     * { "error_message":"Unknown error - Error while updating OEM in WLM Service", "error_code":1002 }
     * 
     * @param oemData record as described
     * @return 
     */
    @PUT
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions("oems:store")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String updateOem(OemData oemData) {
        ValidationUtil.validate(oemData);
        return oemBo.updateOem(oemData, null); // passing in default null for UUID);
    }
    /**
     * Adds the specified Oem in the database. If it can be added a success message
     * is returned. If not, an error message is returned.
     * Sample request:
     * PUT http://localhost:8080/WLMService/resources/oem
     * {"Name":"Oem Name ","Description":"Oem Description"}
     * Sample success output:
     * "true"
     * Sample error output:
     * { "error_message":"Unknown error - Error while creating OEM in WLM Service", "error_code":1002 }
     * 
     * @param oemData record as described
     * @return 
     */
    @POST
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions("oems:create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addOem(OemData oemData) {
        ValidationUtil.validate(oemData);
        log.error("OEM Name: " + oemData.getName());
        log.error("OEM Description: " + oemData.getDescription());
        return oemBo.createOem(oemData, null); // passing in default null for UUID
    }
    
    /**
     * Deletes the specified Oem in the database. If it can be deleted a success message
     * is returned. If not, an error message is returned.
     * Sample request:
     * PUT http://localhost:8080/WLMService/resources/oem
     * {"Name":"Oem Name ","Description":"Oem Description"}
     * Sample success output:
     * "true"
     * Sample error output:
     * { "error_message":"Unknown error - Error while creating OEM in WLM Service", "error_code":1002 }
     * 
     * @param oemData record as described
     * @return 
     */   
    @DELETE
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions("oems:delete")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteOem(@QueryParam("Name")String oemName ) {
        ValidationUtil.validate(oemName);
        return oemBo.deleteOem(oemName, null); // passing in default null for UUID);
    }
    
    

}
