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
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class OemFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<Oem>{
    
    @QueryParam("id")
    public UUID id;
    @QueryParam("nameEqualTo")
    public String nameEqualTo;
    @QueryParam("nameContains")
    public String nameContains;
    
}
