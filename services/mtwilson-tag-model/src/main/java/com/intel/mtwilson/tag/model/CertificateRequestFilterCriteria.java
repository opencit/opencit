/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.FilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class CertificateRequestFilterCriteria implements FilterCriteria<CertificateRequest>{

    @QueryParam("id")
    public UUID id;
    @QueryParam("subjectEqualTo")
    public String subjectEqualTo;
    @QueryParam("subjectContains")
    public String subjectContains;
    @QueryParam("selectionEqualTo")
    public String selectionEqualTo;
    @QueryParam("selectionContains")
    public String selectionContains;
    @QueryParam("statusEqualTo")
    public String statusEqualTo;
    
}
