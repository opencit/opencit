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
public class UserLoginHmacFilterCriteria implements FilterCriteria<UserLoginHmac> {

    @QueryParam("id")
    public UUID id;
    @QueryParam("userIdEqualTo")
    public UUID userIdEqualTo;
}
