/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import com.intel.mtwilson.repository.FilterCriteria;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal, jbuhacoff
 */
public class HostTlsPolicyFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<HostTlsPolicy> {
    
    @QueryParam("id")
    public String id;
    
    /**
     * The mw_tls_policy does not have a hostId field; when this parameter
     * is specified, the repository searches for private=false and name=hostId
     * which is a per-host private record.
     */
    @QueryParam("hostId")
    public String hostId;
    
    @QueryParam("nameEqualTo")
    public String nameEqualTo;

    @QueryParam("nameContains")
    public String nameContains;
    
    @QueryParam("privateEqualTo")
    public Boolean privateEqualTo;

    @QueryParam("commentEqualTo")
    public String commentEqualTo;
    
    @QueryParam("commentContains")
    public String commentContains;
}
