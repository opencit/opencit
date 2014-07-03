/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
<<<<<<< HEAD
import com.intel.mtwilson.repository.FilterCriteria;
=======
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import com.intel.mtwilson.jaxrs2.FilterCriteria;
>>>>>>> bd29d08c853e9e3de3146865a2ce2f02c196172a
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class MlePcrFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<MlePcr> {

    @PathParam("mle_id")
    public UUID mleUuid;
    @QueryParam("id")
    public UUID id;
    @QueryParam("indexEqualTo")
    public String indexEqualTo;
    @QueryParam("valueEqualTo")
    public String valueEqualTo;
    
}
