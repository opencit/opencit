/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.as.rest.v2.model.Mle.MleType;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class MleFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<Mle>{
    
    @QueryParam("id")
    public UUID id;
    @QueryParam("nameEqualTo")
    public String nameEqualTo;
    @QueryParam("nameContains")
    public String nameContains;
    @QueryParam("osUuid")
    public String osUuid;
    @QueryParam("oemUuid")
    public String oemUuid;
    @QueryParam("mleType")
    public MleType mleType;
    
}
