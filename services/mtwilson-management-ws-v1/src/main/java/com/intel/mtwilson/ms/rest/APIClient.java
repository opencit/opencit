/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.ms.business.ApiClientBO;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.ms.common.MSException;
import com.intel.mtwilson.security.annotations.PermitAll;
import com.intel.mtwilson.security.annotations.RolesAllowed;
import com.intel.dcsg.cpg.rfc822.Rfc822Date;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author dsmagadx
 */
@Stateless
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
    @RolesAllowed({"Security"})
    @GET
    @Produces("application/json")
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
    @RolesAllowed({"Security"})
    @PUT
    @Consumes("application/json")
    @Produces(MediaType.TEXT_PLAIN)
    public String updateApiClient(ApiClientUpdateRequest apiClientRequest) {
        ValidationUtil.validate(apiClientRequest);
        new ApiClientBO().update(apiClientRequest);
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
    @RolesAllowed({"Security"})
    @POST
    @Consumes("application/json")
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
    @RolesAllowed({"Security"})
    @GET
    @Produces("application/json")
    public Role[] listAvailableRoles() {
        return new Role[] { Role.Security, Role.Whitelist, Role.Attestation, Role.Report, Role.Audit, Role.AssetTagManagement }; // XXX intentionally omitting the cache role, because we are removing AH from the design, and anyway the cache needs a "real" permission to read whatever it is caching.
    }
    
    
    /**
     * Returns a list of ApiClientInfo based on the search criteria provided.
     * Currently only ONE search criteria at a time is supported. In future
     * versions combinations may be supported (expiresBefore=... and issuerEqualTo...)
     * @return an instance of java.lang.String
     */
    @Path("/search")
    @RolesAllowed({"Security"})
    @GET
    @Produces("application/json")
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
        // first, construct an ApiClientSearchCriteria object. XXX would be nice if we had an automated mapper from query strings to objects like we do from json to objects.
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
    @PermitAll
    @POST
    @Path("/register")
    @Consumes("application/json")
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
    @RolesAllowed({"Security"})
    @DELETE
    public void delete(@QueryParam("fingerprint") String fingerprintHex) {
        ValidationUtil.validate(fingerprintHex);
        byte[] fingerprint = fromHex(fingerprintHex);
        ApiClientBO bo = new ApiClientBO();
        ApiClientInfo info = bo.find(fingerprint);
        // TODO implement this... maybe by setting enabled=0 and status=Deleted ?
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
        apiClientRequest.roles = info.roles;
        bo.update(apiClientRequest);
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
