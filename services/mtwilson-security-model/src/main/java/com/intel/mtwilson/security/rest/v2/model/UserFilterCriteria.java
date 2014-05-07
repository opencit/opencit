/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.security.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.FilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class UserFilterCriteria implements FilterCriteria<User> {

    @QueryParam("id")
    public UUID id;
    @QueryParam("userNameEqualTo")
    public String userNameEqualTo;
}
