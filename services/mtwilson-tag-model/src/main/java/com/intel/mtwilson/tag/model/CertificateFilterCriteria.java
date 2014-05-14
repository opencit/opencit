/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.FilterCriteria;
import java.util.Date;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class CertificateFilterCriteria implements FilterCriteria<Certificate>{

    @QueryParam("id")
    public UUID id;
    @QueryParam("subjectEqualTo")
    public String subjectEqualTo;
    @QueryParam("subjectContains")
    public String subjectContains;
    @QueryParam("issuerEqualTo")
    public String issuerEqualTo;
    @QueryParam("issuerContains")
    public String issuerContains;
    @QueryParam("statusEqualTo")
    public String statusEqualTo;
    @QueryParam("validOn")
    public Date validOn;
    @QueryParam("validBefore")
    public Date validBefore;
    @QueryParam("validAfter")
    public Date validAfter;
    @QueryParam("sha1")
    public Sha1Digest sha1;
    @QueryParam("sha256")
    public Sha256Digest sha256;
    @QueryParam("revoked")
    public Boolean revoked;
    
}
