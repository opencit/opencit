/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.Validator;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class HostAttestationFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<HostAttestation> {
    
    @QueryParam("id")
    public UUID id;
    @QueryParam("host_id")
    public UUID hostUuid;
    @QueryParam("nameEqualTo")
    public String nameEqualTo;
    @QueryParam("aik")
    public String aikSha1;
    @QueryParam("aik_public_key_sha1")
    public String aikPublicKeySha1;
    @QueryParam("numberOfDays")
    public int numberOfDays;
    @QueryParam("fromDate")
    @Regex(RegexPatterns.ANY_VALUE)    
    public String fromDate;
    @QueryParam("toDate")
    @Regex(RegexPatterns.ANY_VALUE)
    public String toDate;
    
}
