/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.mtwilson.jersey.FilterCriteria;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class HostAikFilterCriteria implements FilterCriteria<HostAik> {
    
    @PathParam("host_id")
    public String hostUuid;
    @QueryParam("id")
    public String aikSha1;
    
}
