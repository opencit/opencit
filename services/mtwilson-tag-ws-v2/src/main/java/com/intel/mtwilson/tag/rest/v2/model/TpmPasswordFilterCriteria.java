/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.FilterCriteria;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class TpmPasswordFilterCriteria implements FilterCriteria<TpmPassword>{
    
    @PathParam("id")
    public UUID id;
    
}
