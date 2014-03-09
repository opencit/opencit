/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.FilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class CertificateFilterCriteria implements FilterCriteria<Certificate>{

    @QueryParam("id")
    public UUID id;
    @QueryParam("subjectEqualTo")
    public UUID subjectEqualTo;
    
}
