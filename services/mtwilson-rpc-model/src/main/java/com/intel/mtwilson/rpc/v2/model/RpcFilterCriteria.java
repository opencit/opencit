/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.FilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author jbuhacoff
 */
public class RpcFilterCriteria implements FilterCriteria<Rpc> {
    
    @QueryParam("id")
    public UUID id;
    @QueryParam("nameEqualTo")
    public String nameEqualTo;
    @QueryParam("nameContains")
    public String nameContains;
    
    @QueryParam("statusEqualTo")
    public Rpc.Status status;
    
    // TODO also allow administrators to filter by user,
    // createdOn, updatedOn, whether it has inline input or a link to a file,
    // current progress, or status
}
