/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author jbuhacoff
 */
public class MleSourceLocator implements Locator<MleSource> {

    @PathParam("mle_id")
    public UUID mleUuid;
    @PathParam("id")
    public UUID id;
    
    @Override
    public void copyTo(MleSource item) {
        if( id != null ) {
            item.setId(id);
        }
        if( mleUuid != null ) {
            item.setMleUuid(mleUuid.toString());
        }  
    }
    
}
