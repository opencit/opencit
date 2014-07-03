/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import com.intel.mtwilson.repository.FilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class RoleFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<Role> {

    @QueryParam("id")
    public UUID id;
    @QueryParam("nameEqualTo")
    public String nameEqualTo;
    @QueryParam("nameContains")
    public String nameContains;
}
