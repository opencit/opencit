/*
 * Copyright (C) 2013 Intel Corporation
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
public class HostLocator implements Locator<Host> {

    @PathParam("id")
    public UUID id;

    @Override
    public void copyTo(Host item) {
        if( id != null ) {
            item.setId(id);
        }
    }
    
}
