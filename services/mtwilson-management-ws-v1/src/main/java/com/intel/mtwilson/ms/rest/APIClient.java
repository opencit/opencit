/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.ms.business.ApiClientBO;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.security.annotations.PermitAll;
import com.intel.mtwilson.security.annotations.RolesAllowed;
import com.intel.dcsg.cpg.rfc822.Rfc822Date;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.launcher.ws.ext.V1;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
//import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author dsmagadx
 */
@V1
//@Stateless
@Path("/ManagementService/resources/apiclient")
public class APIClient {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor must be public for framework to instantiate this REST API.
     */
    public APIClient() {
    }

  
    /**
     * Retrieves representation of an instance of com.intel.mountwilson.ms.rest.APIClient
     * @return an instance of java.lang.String
     */
    //@RolesAllowed({"Security"})
    @RequiresPermissions("users:retrieve")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ApiClientInfo getApiClientInfo(@QueryParam("fingerprint") String fingerprintHex) {
        ValidationUtil.validate(fingerprintHex);
        byte[] fingerprint = fromHex(fingerprintHex);
        return new ApiClientBO().find(fingerprint);
    }

    /**
     * PUT method for updating or creating an instance of APIClient
     * @param content representation for the resource
     * @return 
     */
    //@RolesAllowed({"Security"})
    @RequiresPermissions({"users:store"})
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String updateApiClient(ApiClientUpdateRequest apiClientRequest) {
        ValidationUtil.validate(apiClientRequest);
        new ApiClientBO().update(apiClientRequest, null);
        return "OK";
    }
    /**
     * POST method for creating an instance of APIClient. The
     * difference between this method and registerApiClient is that
     * this method allows an administrator to add arbitrary credentials
     * and this request is authenticated and requires the Security role,
     * whereas registerApiClient is a public API (non-authenticated) and
     * must be self-signed.
     * @param ApiClientCreateRequest
     * @return 
     */
    //@RolesAllowed({"Security"})
    @RequiresPermissions("users:create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addApiClient(ApiClientCreateRequest apiClientRequest) {
        ValidationUtil.validate(apiClientRequest);
        new ApiClientBO().create(apiClientRequest);
        return "OK";
    }

    /**
     * Retrieves representation of an instance of com.intel.mountwilson.ms.rest.APIClient
     * @return an instance of java.lang.String
     */
    @Path("/availableRoles")
    @RequiresPermissions("users:search")   
    //@RolesAllowed({"Security"})
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Role[] listAvailableRoles() {
        return new Role[] { Role.Security, Role.Whitelist, Role.Attestation, Role.Report, Role.Audit, Role.AssetTagManagement,
        Role.Administrator, Role.AssetTagManager, Role.Auditor, Role.Challenger, Role.HostManager, Role.ReportManager, 
        Role.ServerManager, Role.UserManager, Role.WhitelistManager, Role.TlsPolicyManager}; 
    }
    
    
    /**
     * Returns a list of ApiClientInfo based on the search criteria provided.
     * Currently only ONE search criteria at a time is supported. In future
     * versions combinations may be supported (expiresBefore=... and issuerEqualTo...)
     * @return an instance of java.lang.String
     */
    @Path("/search")
    //@RolesAllowed({"Security"})
    @RequiresPermissions("users:search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiClientInfo> searchApiClientInfo(
            @QueryParam("enabledEqualTo") String enabledEqualTo,
            @QueryParam("expiresAfter") String expiresAfter,
            @QueryParam("expiresBefore") String expiresBefore,
            @QueryParam("fingerprintEqualTo") String fingerprintEqualTo,
            @QueryParam("issuerEqualTo") String issuerEqualTo,
            @QueryParam("nameContains") String nameContains,
            @QueryParam("nameEqualTo") String nameEqualTo,
            @QueryParam("serialNumberEqualTo") String serialNumberEqualTo,
            @QueryParam("statusEqualTo") String statusEqualTo
            ) {
        // first, construct an ApiClientSearchCriteria object. 
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ApiClientSearchCriteria criteria = new ApiClientSearchCriteria();
        try {
            if( enabledEqualTo != null ) {
                ValidationUtil.validate(enabledEqualTo);
                criteria.enabledEqualTo = Boolean.valueOf(enabledEqualTo); // deserialize from "true" or "false"
            }
            if( expiresAfter != null ) {
                ValidationUtil.validate(expiresAfter);
                criteria.expiresAfter = dateFormat.parse(expiresAfter);
            }
            if( expiresBefore != null ) {
                ValidationUtil.validate(expiresBefore);
                criteria.expiresBefore = dateFormat.parse(expiresBefore);
            }
            if( fingerprintEqualTo != null ) {
                ValidationUtil.validate(fingerprintEqualTo);
                criteria.fingerprintEqualTo = Hex.decodeHex(fingerprintEqualTo.toCharArray());
            }
            if( issuerEqualTo != null ) {
                ValidationUtil.validate(issuerEqualTo);
                criteria.issuerEqualTo = issuerEqualTo;
            }
            if( nameContains != null ) {
                ValidationUtil.validate(nameContains);
                criteria.nameContains = nameContains;
            }
            if( nameEqualTo != null ) {
                ValidationUtil.validate(nameEqualTo);
                criteria.nameEqualTo = nameEqualTo;
            }
            if( serialNumberEqualTo != null ) {
                ValidationUtil.validate(serialNumberEqualTo);
                criteria.serialNumberEqualTo = Integer.valueOf(serialNumberEqualTo);
            }
            if( statusEqualTo != null ) {
                ValidationUtil.validate(statusEqualTo);
                criteria.statusEqualTo = statusEqualTo;
            }
        }
        catch(ParseException e) {
            log.error("Error parsing input.", e);
            throw new MSException(ErrorCode.MS_ERROR_PARSING_INPUT, "Cannot parse date: "+e.getMessage()+" (should be yyyy-MM-dd)");
        }
        catch(DecoderException e) {
            log.error("Error parsing input.", e);
            throw new MSException(ErrorCode.MS_ERROR_PARSING_INPUT, "Cannot parse fingerprint: "+e.getMessage()+" (should be hex)");
        }
        return new ApiClientBO().search(criteria);
    }
    
    
    /**
     * POST method for creating an instance of APIClient whose status is
     * "Pending" and is disabled. Then a user can approve it via the 
     * management application. The
     * difference between this method and addApiClient is that addApiClient
     * allows an administrator to add arbitrary credentials and requires the
     * Security role, where as this method is a public API (non-authenticated) and
     * must be self-signed.
     * @param ApiClientCreateRequest
     * @return 
     */
    //@PermitAll - If nothing is specified, then the access is open to everyone
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String registerApiClient(ApiClientCreateRequest apiClientRequest) {
        ValidationUtil.validate(apiClientRequest);
        log.debug("API client registration: {}", Base64.encodeBase64String(apiClientRequest.getCertificate()));
        new ApiClientBO().create(apiClientRequest);
        return "OK";
    }

    

    /**
     * DELETE method for resource APIClient
     */
    //@RolesAllowed({"Security"})
    @RequiresPermissions("users:delete")
    @DELETE
    public void delete(@QueryParam("fingerprint") String fingerprintHex) {
        ValidationUtil.validate(fingerprintHex);
        byte[] fingerprint = fromHex(fingerprintHex);
        ApiClientBO bo = new ApiClientBO();
        
        ApiClientSearchCriteria criteria = new ApiClientSearchCriteria();
        criteria.fingerprintEqualTo = fingerprint;
        List<ApiClientInfo> userList = bo.search(criteria);
        if (userList != null && userList.size() == 1) {
            log.debug("Found the user to delete.");
            ApiClientInfo info = userList.get(0);
            //ApiClientInfo info = bo.find(fingerprint);
            ApiClientUpdateRequest apiClientRequest = new ApiClientUpdateRequest();
            apiClientRequest.fingerprint = fingerprint;
            apiClientRequest.enabled = false;
            apiClientRequest.status = ApiClientStatus.CANCELLED.toString();
            if (info.comment == null || info.comment.isEmpty()){
                apiClientRequest.comment = String.format("Deleted on %s", Rfc822Date.format(new Date()));
            }
            else{
                apiClientRequest.comment = String.format("%s. Deleted on %s", info.comment, Rfc822Date.format(new Date()));
            }
            // Removing the roles as well
            apiClientRequest.roles = new String[]{}; //info.roles; 
            bo.delete(apiClientRequest, null);
        } else {
            log.debug("Did not find the user with fingerprint {} in the system. ", fingerprintHex);
        }
    }
    
    private byte[] fromHex(String hex) {
        try {
            return Hex.decodeHex(hex.toCharArray());
        } catch (DecoderException ex) {
            log.error("Error parsing input.", ex);
            throw new MSException(ErrorCode.MS_ERROR_PARSING_INPUT, "Invalid fingerprint");
        }        
    }
}
