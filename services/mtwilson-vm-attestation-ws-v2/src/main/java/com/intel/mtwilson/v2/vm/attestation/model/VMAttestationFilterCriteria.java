/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.v2.vm.attestation.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class VMAttestationFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<VMAttestation> {
    
    @QueryParam("hostNameEqualTo")
    public String hostName;
    @QueryParam("vmInstanceIdEqualTo")
    public String vmInstanceId;
    
}
