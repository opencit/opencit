/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.shiro.jdbi.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.FilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class RolePermissionFilterCriteria implements FilterCriteria<RolePermission> {

    @QueryParam("id")
    public UUID id;
    @QueryParam("domainEqualTo")
    public String domainEqualTo;
    @QueryParam("actionEqualTo")
    public String actionEqualTo;
}
