/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.intel.mtwilson.wlm.rest;

import com.intel.mtwilson.wlm.business.MleBO;
import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.datatypes.MleSource;
import com.intel.mtwilson.datatypes.PCRWhiteList;
import com.intel.mtwilson.datatypes.ModuleWhiteList;
//import com.intel.mountwilson.wlm.rest.data.ModuleWhiteListData;
//import com.intel.mountwilson.wlm.rest.data.PCRWhiteListData;
import java.util.List;
//import javax.annotation.security.RolesAllowed;
import com.intel.mtwilson.security.annotations.*;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.launcher.ws.ext.V1;

//import javax.ejb.Stateless;
//import javax.ejb.TransactionAttribute;
//import javax.ejb.TransactionAttributeType;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * REST Web Service
 *
 * @author mkuchtiak
 */
@V1
//@Stateless
@Path("/WLMService/resources/mles")
public class Mle {

    MleBO mleBO = new MleBO();

    /**
     * Adds the specified MLE to the database. If it can be added a success message
     * is returned. If not, an error message is returned.
     * Sample request:
     * POST http://localhost:8080/WLMService/resources/mles
     * {"Name":"OEM MLE A","Description":"OEM MLE Revised","Attestation_Type":"PCR","MLE_Manifests":[{"Name":"1","Value":"abcdefghijklmnop"},{"Name":"2","Value":"jklmnopabcdefghi"}],"MLE_Type":"VMM","Version":"1.2.3"}
     * Sample success output:
     * "true"
     * Sample error output:
     * { "error_message":"Unknown error - Error while creating MLE in WLM Service", "error_code":1002 }
     * 
     * @param mleData record as described
     * @return 
     */
    @POST
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:create","mle_pcrs:create","mle_sources:create"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addMle(MleData mleData) {
        ValidationUtil.validate(mleData);
        return mleBO.addMLe(mleData, null);
    }

    /**
     * Updates the specified MLE to the database. If it can be updated a success message
     * is returned. If not, an error message is returned.
     * Sample request:
     * PUT http://localhost:8080/WLMService/resources/mles
     * {"Name":"OEM MLE A","Description":"OEM MLE Revised","Attestation_Type":"PCR","MLE_Manifests":[{"Name":"1","Value":"abcdefghijklmnop"},{"Name":"2","Value":"jklmnopabcdefghi"}],"MLE_Type":"VMM","Version":"1.2.3"}
     * Sample success output:
     * "true"
     * Sample error output:
     * { "error_message":"Unknown error - Error while creating MLE in WLM Service", "error_code":1002 }
     * 
     * @param mleData record as described
     * @return 
     */
    @PUT
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:store","mle_pcrs:create,store","mle_sources:create,store"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String updateMle(MleData mleData) {
        ValidationUtil.validate(mleData);
        return mleBO.updateMle(mleData, null);
    }

    /**
     * Returns the name, version, MLE type, description, attestation type, and manifests (list) for all MLEs that
     * match the search criteria.
     * 
     * Searches for all MLEs with a name matching the search term. For example,
     * if the database contains MLE with name "OEM SW A" and "OEM SW B"
     * then a searchCriteria of "OEM" would return both, whereas "SW A" would
     * return only "OEM SW A".
     * 
     * Sample request:
     * http://localhost:8080/WLMService/resources/mles?searchCriteria=EPSD
     * Sample output:
     * [
     *   {"Name":"EPSD","Version":"55","MLE_Type":"BIOS","Description":"","Attestation_Type":"PCR",
     *    "MLE_Manifests":[{"Name":"0","Value":"E3A29BD603BF9982113B696CD37AF8AFC58E2877"}]},
     *   {"Name":"EPSD","Version":"60","MLE_Type":"BIOS","Description":"","Attestation_Type":"PCR",
     *    "MLE_Manifests":[{"Name":"0","Value":"5E724D834FEC48C62D523D95D08884DCAC7F4F98"}]},
     *   {"Name":"EPSD","Version":"58","MLE_Type":"BIOS","Description":"","Attestation_Type":"PCR",
     *    "MLE_Manifests":[{"Name":"0","Value":"365A73E405821F88A68346E73F2FDA1215C03696"}]}
     * ]
     * 
     * @param searchCriteria a portion of the MLE name to search. 
     * @return 
     */
    @GET
    //@RolesAllowed({"Whitelist"})
//    @Consumes(MediaType.TEXT_HTML)
    @RequiresPermissions({"mles:search","mle_pcrs:search","mle_sources:search"})
    @Produces(MediaType.APPLICATION_JSON)
    public List<MleData> queryForMLE(@QueryParam("searchCriteria") String searchCriteria) {
        ValidationUtil.validate(searchCriteria);
        return mleBO.listMles(searchCriteria);
    }

    /**
     * Returns the name, version, MLE type, description, attestation type, and manifests (list) for the specified MLE.
     * Sample request:
     * GET http://localhost:8080/WLMService/resources/mles/manifest?mleName=EPSD&mleVersion=60
     * Sample response:
     * {"Name":"EPSD","Version":"60","MLE_Type":"BIOS","Description":"","Attestation_Type":"PCR","MLE_Manifests":[{"Name":"0","Value":"5E724D834FEC48C62D523D95D08884DCAC7F4F98"}]}
     * 
     * @param mleName
     * @param mleVersion
     * @return 
     */
    @GET
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:retrieve","mle_pcrs:retrieve","mle_sources:retrieve"})    
    @Path("/manifest")
//    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.APPLICATION_JSON)
    public MleData getMLEDetails(
            @QueryParam("mleName") String mleName,
            @QueryParam("mleVersion") String mleVersion,
            @QueryParam("osName") String osName,
            @QueryParam("osVersion") String osVersion,
            @QueryParam("oemName") String oemName) {
        
        ValidationUtil.validate(mleName);
        ValidationUtil.validate(mleVersion);
        ValidationUtil.validate(osName);
        ValidationUtil.validate(osVersion);
        ValidationUtil.validate(oemName);
        
        return mleBO.findMle(mleName, mleVersion, osName, osVersion, oemName);
    }

    /**
     * Deletes an MLE from the database. The MLE is specified by name and version.
     * If successful, the string "true" will be returned.
     * 
     * Sample request:
     * DELETE http://localhost:8080/WLMService/resources/mles?mleName=EPSD&mleVersion=60
     * Sample response:
     * "true"
     * 
     * @param mleName
     * @param mleVersion
     * @return 
     */
    @DELETE
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:delete","mle_pcrs:delete","mle_sources:delete","mle_modules:delete"})    
//    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteMle(
            @QueryParam("mleName") String mleName, 
            @QueryParam("mleVersion") String mleVersion,
            @QueryParam("osName") String osName,
            @QueryParam("osVersion") String osVersion,
            @QueryParam("oemName") String oemName) {
        
        ValidationUtil.validate(mleName);
        ValidationUtil.validate(mleVersion);
        ValidationUtil.validate(osName);
        ValidationUtil.validate(osVersion);
        ValidationUtil.validate(oemName);
        
        return mleBO.deleteMle(mleName, mleVersion,osName, osVersion, oemName, null);
    }
    
    /**
     * Added By: Sudhir on June 20, 2012
     * 
     * Process the add request into the PCR manifest table.
     * 
     * @param pcrData : White List data to be added to the PCR Manifest table
     * @return : "true" if success or else exception.
     */
    @POST
    //@RolesAllowed({"Whitelist"})
    @Path("/whitelist/pcr")
    @RequiresPermissions({"mles:retrieve","mle_pcrs:create"})    
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addPCRWhiteList(PCRWhiteList pcrData) {
        ValidationUtil.validate(pcrData);
        return mleBO.addPCRWhiteList(pcrData);
    }

    /**
     * Added By: Sudhir on June 20, 2012
     * 
     * Processes the update request into the PCR manifest table.
     * 
     * @param pcrData : White List data to be updated in the PCR Manifest table
     * @return : "true" if success or else exception.
     */
    @PUT
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:retrieve","mle_pcrs:store"})    
    @Path("/whitelist/pcr")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String updatePCRWhiteList(PCRWhiteList pcrData) {
        ValidationUtil.validate(pcrData);
        return mleBO.updatePCRWhiteList(pcrData);
    }
    
    /**
     * Added By: Sudhir on June 20, 2012
     * 
     * Processes the delete request from the PCR manifest table.
     * 
     * @param pcrName : Name of the PCR entry that needs to be deleted.
     * @param mleName : Name of the measured launch environment (MLE) associated with the white list.
     * @param mleVersion : Version of the MLE or Hypervisor
     * @param osName : Name of the OS running the hypervisor. OS Details need to be provided only
     * when the associated MLE is of VMM type.
     * @param osVersion : Version of the OS
     * @param oemName : OEM vendor of the hardware system. OEM Details have to be provided only 
     * when the associated MLE is of BIOS type.
     * @return : "true" if success or else exception.
     */
    @DELETE
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:retrieve","mle_pcrs:delete"})    
    @Path("/whitelist/pcr")
    @Produces(MediaType.TEXT_PLAIN)
    public String deletePCRWhiteList(
            @QueryParam("pcrName") String pcrName,
            @QueryParam("mleName") String mleName, 
            @QueryParam("mleVersion") String mleVersion,
            @QueryParam("osName") String osName,
            @QueryParam("osVersion") String osVersion,
            @QueryParam("oemName") String oemName) {
        
        ValidationUtil.validate(pcrName);
        ValidationUtil.validate(mleName);
        ValidationUtil.validate(mleVersion);
        ValidationUtil.validate(osName);
        ValidationUtil.validate(osVersion);
        ValidationUtil.validate(oemName);
        
        return mleBO.deletePCRWhiteList(pcrName, "SHA1", mleName, mleVersion,osName, osVersion, oemName, null);
    }

     /**
     * Added By: Sudhir on June 21, 2012
     * 
     * Process the add request of the white list into the Module manifest table.
     * 
     * @param moduleData : White List data to be added to the Module Manifest table
     * @return : "true" if success or else exception.
     */
    @POST
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:retrieve","mle_modules:create"})    
    @Path("/whitelist/module")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addModuleWhiteList(ModuleWhiteList moduleData) {
        ValidationUtil.validate(moduleData);
        return mleBO.addModuleWhiteList(moduleData);
    }

    /**
     * Added By: Sudhir on June 21, 2012
     * 
     * Process the update request of the module manifest entry.
     * 
     * @param moduleData : Module manifest entry details that needs to be updated
     * @return : "true" if success or else exception.
     */
    @PUT
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:retrieve","mle_modules:store"})    
    @Path("/whitelist/module")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String updateModuleWhiteList(ModuleWhiteList moduleData) {
        ValidationUtil.validate(moduleData);
        return mleBO.updateModuleWhiteList(moduleData);
    }

    /**
     * Added By: Sudhir on June 21, 2012
     * 
     * Deleted the specified module manifest entity from the module manifest table
     * 
     * @param componentName : Name of the module/component
     * @param eventName : Event associated with the component
     * @param mleName : Name of the measured launch environment (MLE) associated with the white list.
     * @param mleVersion : Version of the MLE or Hypervisor
     * @param osName : Name of the OS running the hypervisor. OS Details need to be provided only
     * when the associated MLE is of VMM type.
     * @param osVersion : Version of the OS
     * @param oemName : OEM vendor of the hardware system. OEM Details have to be provided only 
     * when the associated MLE is of BIOS type.
     * @return : "true" if success or else exception.
     */
    @DELETE
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:retrieve","mle_modules:delete"})    
    @Path("/whitelist/module")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteModuleWhiteList(
            @QueryParam("componentName") String componentName,
            @QueryParam("eventName") String eventName,
            @QueryParam("pcrBank") String pcrBank,
            @QueryParam("mleName") String mleName, 
            @QueryParam("mleVersion") String mleVersion,
            @QueryParam("osName") String osName,
            @QueryParam("osVersion") String osVersion,
            @QueryParam("oemName") String oemName) {
        
        ValidationUtil.validate(componentName);
        ValidationUtil.validate(eventName);
        ValidationUtil.validate(pcrBank);
        ValidationUtil.validate(mleName);
        ValidationUtil.validate(mleVersion);
        ValidationUtil.validate(osName);
        ValidationUtil.validate(osVersion);
        ValidationUtil.validate(oemName);
        
        return mleBO.deleteModuleWhiteList(componentName, eventName, pcrBank, mleName, mleVersion, osName, osVersion, oemName, null);
    }

    /**
     * Added By: Sudhir on June 21, 2012     
     * 
     * Retrieves the list of module white lists for the specified MLE.
     * 
     * @param mleName : Name of the measured launch environment (MLE) associated with the white list.
     * @param mleVersion : Version of the MLE or Hypervisor
     * @param osName : Name of the OS running the hypervisor. OS Details need to be provided only
     * when the associated MLE is of VMM type.
     * @param osVersion : Version of the OS
     * @param oemName : OEM vendor of the hardware system. OEM Details have to be provided only 
     * when the associated MLE is of BIOS type.
     * @return : List of module white lists.
     */
    @GET
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:retrieve","mle_modules:retrieve"})    
    @Path("/whitelist/module")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ModuleWhiteList> getModuleWhiteList(
            @QueryParam("mleName") String mleName, 
            @QueryParam("mleVersion") String mleVersion,
            @QueryParam("osName") String osName,
            @QueryParam("osVersion") String osVersion,
            @QueryParam("oemName") String oemName) {
        
        ValidationUtil.validate(mleName);
        ValidationUtil.validate(mleVersion);
        ValidationUtil.validate(osName);
        ValidationUtil.validate(osVersion);
        ValidationUtil.validate(oemName);
        
        return mleBO.getModuleWhiteList(mleName, mleVersion, osName, osVersion, oemName);
    }

    @POST
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:retrieve","mle_sources:create"})        
    @Path("/source")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addMleSource(MleSource mleSourceObj) {
        ValidationUtil.validate(mleSourceObj);
        return mleBO.addMleSource(mleSourceObj, null, null);
    }

    @PUT
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:retrieve","mle_sources:store"})        
    @Path("/source")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String updateMleSource(MleSource mleSourceObj) {
        ValidationUtil.validate(mleSourceObj);
        return mleBO.updateMleSource(mleSourceObj, null);
    }

    @DELETE
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:retrieve","mle_sources:delete"})        
    @Path("/source")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteMleSource(
            @QueryParam("mleName") String mleName, 
            @QueryParam("mleVersion") String mleVersion,
            @QueryParam("osName") String osName,
            @QueryParam("osVersion") String osVersion,
            @QueryParam("oemName") String oemName) {
        
        ValidationUtil.validate(mleName);
        ValidationUtil.validate(mleVersion);
        ValidationUtil.validate(osName);
        ValidationUtil.validate(osVersion);
        ValidationUtil.validate(oemName);
        
        return mleBO.deleteMleSource(mleName, mleVersion, osName, osVersion, oemName, null);
    }

    @GET
    //@RolesAllowed({"Whitelist"})
    @RequiresPermissions({"mles:retrieve","mle_sources:retrieve"})        
    @Path("/source")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMleSource(
            @QueryParam("mleName") String mleName, 
            @QueryParam("mleVersion") String mleVersion,
            @QueryParam("osName") String osName,
            @QueryParam("osVersion") String osVersion,
            @QueryParam("oemName") String oemName) {

        ValidationUtil.validate(mleName);
        ValidationUtil.validate(mleVersion);
        ValidationUtil.validate(osName);
        ValidationUtil.validate(osVersion);
        ValidationUtil.validate(oemName);
        
        return mleBO.getMleSource(mleName, mleVersion, osName, osVersion, oemName);
    }
    
}
