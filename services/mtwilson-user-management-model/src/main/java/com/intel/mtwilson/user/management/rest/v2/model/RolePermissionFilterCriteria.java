/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.RegexPatterns;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class RolePermissionFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<RolePermission> {

    @PathParam("role_id")
    public UUID roleId;
    @Regex(RegexPatterns.ANY_VALUE)
    @QueryParam("domainEqualTo")
    public String domainEqualTo;
    @Regex(RegexPatterns.ANY_VALUE)
    @QueryParam("actionEqualTo")
    public String actionEqualTo;
}
