/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.FilterCriteria;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class MleModuleFilterCriteria implements FilterCriteria<MleModule> {

    @PathParam("mle_id")
    public UUID mleUuid;
    @QueryParam("id")
    public UUID id;
    @QueryParam("nameContains")
    public String nameContains;
    @QueryParam("valueEqualTo")
    public String valueEqualTo;
    
}
