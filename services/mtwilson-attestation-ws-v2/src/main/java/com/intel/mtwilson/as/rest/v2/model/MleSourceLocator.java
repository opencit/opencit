/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author jbuhacoff
 */
public class MleSourceLocator implements Locator<MleSource> {
    @PathParam("id")
    public UUID id;
    @PathParam("mle")
    public String mleUuid;
    
    @Override
    public void copyTo(MleSource item) {
        item.setId(id);
        item.setMleUuid(mleUuid);
    }
    
}
