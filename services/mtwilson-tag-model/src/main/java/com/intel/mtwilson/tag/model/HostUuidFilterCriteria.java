/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class HostUuidFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<HostUuid>{
    
    @QueryParam("id")
    public UUID id;
    @QueryParam("hostId")
    public String hostId;
    @QueryParam("hostNameEqualTo")
    public String hostNameEqualTo;
    
}
