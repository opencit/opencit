/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.FilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class UserLoginCertificateFilterCriteria implements FilterCriteria<UserLoginCertificate> {

    @QueryParam("id")
    public UUID id;
    @QueryParam("userIdEqualTo")
    public UUID userIdEqualTo;
    @QueryParam("userNameEqualTo")
    public String userNameEqualTo;
    @QueryParam("sha1")
    public byte[] sha1;
    @QueryParam("sha256")
    public byte[] sha256;
    
}
