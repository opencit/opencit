/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tpm.endorsement.model;

import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import com.intel.mtwilson.repository.FilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author jbuhacoff
 */
public class TpmEndorsementFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<TpmEndorsement> {
    
    @QueryParam("id")
    public String id;
    
    @QueryParam("hardwareUuidEqualTo")
    public String hardwareUuidEqualTo;
    
//    @QueryParam("publicKeySha256EqualTo")
//    public String publicKeySha256EqualTo;
    
    @QueryParam("issuerEqualTo")
    public String issuerEqualTo;

    @QueryParam("issuerContains")
    public String issuerContains;
    
    @QueryParam("revokedEqualTo")
    public Boolean revokedEqualTo;

    @QueryParam("commentEqualTo")
    public String commentEqualTo;
    
    @QueryParam("commentContains")
    public String commentContains;
}
