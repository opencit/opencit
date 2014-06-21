/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import com.intel.mtwilson.jaxrs2.FilterCriteria;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class HostTlsPolicyFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<HostTlsPolicy> {
    
    @PathParam("host_id")
    public UUID hostUuid;    
    
}
