/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest;

import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import com.intel.mtwilson.security.annotations.RolesAllowed;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@Stateless
@Path("/assetTagCert")
public class AssetTagCert {
 private Logger log = LoggerFactory.getLogger(getClass());
    public AssetTagCert() {
    }
    
    /**
     * This REST API would be called by the tag provisioning service whenever a new asset tag certificate is generated for a host.
     * Initially we would stored this asset tag certificate in the DB without being mapped to any host. After the host is registered, then
     * the asset tag certificate would be mapped to it.
     * @param atagObj
     * @return 
     */
    @RolesAllowed({"AssetTagManagement"})
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String importAssetTagCertificate(AssetTagCertCreateRequest atagObj)  {
        boolean result = true;
        return Boolean.toString(result);
    }
    
    /**
     * This REST API would be called by tag provisioning service whenever a valid asset tag certificate is revoked.
     * @param atagObj
     * @return 
     */
    @RolesAllowed({"AssetTagManagement"})
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String revokeAssetTagCertificate(AssetTagCertRevokeRequest atagObj)  {
        boolean result = true;
        return Boolean.toString(result);
    }
    
}
