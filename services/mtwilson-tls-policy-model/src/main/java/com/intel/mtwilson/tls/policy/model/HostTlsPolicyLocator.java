/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal, jbuhacoff
 */
public class HostTlsPolicyLocator implements Locator<HostTlsPolicy> {

    @PathParam("id")
    public UUID id;

    @Override
    public void copyTo(HostTlsPolicy item) {
        if( id != null ) {
            item.setId(id);
        }        
    }
    
}
