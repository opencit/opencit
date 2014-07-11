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
public class SelectionFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<Selection>{
    
    @QueryParam("id")
    public UUID id;
    @QueryParam("nameEqualTo")
    public String nameEqualTo;
    @QueryParam("nameContains")
    public String nameContains; // the name of the selection
    @QueryParam("descriptionEqualTo")
    public String descriptionEqualTo; // one or more subjects (hosts) included in the selection
    @QueryParam("descriptionContains")
    public String descriptionContains; // one or more subjects (hosts) included in the selection
    
}
