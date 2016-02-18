/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class VMAttestationFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<VMAttestation> {
    
    @QueryParam("id")
    public UUID id;
    @QueryParam("hostNameEqualTo")
    public String hostName;
    @QueryParam("vmInstanceIdEqualTo")
    public String vmInstanceId;
    @QueryParam("numberOfDays")
    public int numberOfDays;
    @QueryParam("fromDate")
    @Regex(RegexPatterns.ANY_VALUE)    
    public String fromDate;
    @QueryParam("toDate")
    @Regex(RegexPatterns.ANY_VALUE)
    public String toDate;
    
}
