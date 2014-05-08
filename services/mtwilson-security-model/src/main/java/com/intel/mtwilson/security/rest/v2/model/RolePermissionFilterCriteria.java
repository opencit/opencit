/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.security.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.DefaultFilterCriteria;
import com.intel.mtwilson.jersey.FilterCriteria;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class RolePermissionFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<RolePermission> {

    @PathParam("role_id")
    public UUID roleId;
    @QueryParam("id")
    public UUID id;
    @QueryParam("domainEqualTo")
    public String domainEqualTo;
    @QueryParam("actionEqualTo")
    public String actionEqualTo;
}
