/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author jbuhacoff
 */
public class RpcLocator implements Locator<Rpc> {
    @PathParam("id")
    public UUID id;
    
    @Override
    public void copyTo(Rpc item) {
        item.setId(id);
    }
    
}
