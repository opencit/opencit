/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.FilterCriteria;
import java.util.Date;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class UserCertificateFilterCriteria implements FilterCriteria<UserCertificate>{
    
    @PathParam("user_id")
    public UUID userUuid;
    @QueryParam("id")
    public UUID id;
    @QueryParam("nameEqualTo")
    public String nameEqualTo;
    @QueryParam("nameContains")
    public String nameContains;
    @QueryParam("enabled")
    public Boolean enabled;
    @QueryParam("expiresAfter")
    public Date expiresAfter;
    @QueryParam("expiresBefore")
    public Date expiresBefore;
    @QueryParam("status")
    public String status;
    @QueryParam("fingerprint")
    public byte[] fingerprint;
    
    
}
