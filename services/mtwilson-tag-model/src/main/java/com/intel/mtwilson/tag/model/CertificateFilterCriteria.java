/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import java.util.Date;
import javax.ws.rs.QueryParam;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.Unchecked;


/**
 *
 * @author ssbangal
 */
public class CertificateFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<Certificate>{

    @QueryParam("id")
    public UUID id;
    @QueryParam("subjectEqualTo")
    public String subjectEqualTo;
    @QueryParam("subjectContains")
    public String subjectContains;
    @QueryParam("issuerEqualTo")
    @Regex(RegexPatterns.ANY_VALUE)    
    public String issuerEqualTo;
    @QueryParam("issuerContains")
    @Regex(RegexPatterns.ANY_VALUE)    
    public String issuerContains;
    @QueryParam("statusEqualTo")
    public String statusEqualTo;
    @QueryParam("validOn")
    public Date validOn;
    @QueryParam("validBefore")
    public Date validBefore;
    @QueryParam("validAfter")
    public Date validAfter;
    @Unchecked
    @QueryParam("sha1")
    public Sha1Digest sha1;
    @Unchecked
    @QueryParam("sha256")
    public Sha256Digest sha256;
    @QueryParam("revoked")
    public Boolean revoked;
    
}
