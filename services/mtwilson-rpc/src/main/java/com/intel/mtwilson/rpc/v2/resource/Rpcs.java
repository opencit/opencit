/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcFilterCriteria;
import com.intel.mtwilson.rpc.v2.model.RpcCollection;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

/**
 *
 * @author jbuhacoff
 */
@V2
@Stateless
@Path("/rpc")
public class Rpcs extends AbstractResource<Rpc,RpcCollection,RpcFilterCriteria,NoLinks<Rpc>> {

    @Override
    protected RpcCollection search(RpcFilterCriteria criteria) {
        RpcCollection rpcs = new RpcCollection();
        Rpc rpc = new Rpc();
        rpc.setName("testrpc");
//        rpc.setContentType("text/plain");
//        rpc.setContent("hello world");
        rpcs.getRpcs().add(rpc);
        return rpcs;
    }

    @Override
    protected Rpc retrieve(String id) {
        if( !UUID.isValid(id) ) { return null; } // XXX TODO or a localizable input validation error via throw exception (if we don't validate here, UUID.valueOf() will throw IllegalArgumentException if it's not a valid uuid)
        Rpc rpc = new Rpc();
        rpc.setId(UUID.valueOf(id));
        rpc.setName("testrpc");
//        rpc.setContentType("text/plain");
//        rpc.setContent("hello world");
        return rpc;
    }

    @Override
    protected void create(Rpc item) {
        // store it...
    }
    @Override
    protected void store(Rpc item) {
        // store it...
    }

    @Override
    protected void delete(String id) {
    }

    /*
    @Override
    protected RpcFilterCriteria createFilterCriteriaWithId(String id) {
        RpcFilterCriteria criteria = new RpcFilterCriteria();
        criteria.id = UUID.valueOf(id);
        return criteria;
    }
    */
    @Override
    protected RpcCollection createEmptyCollection() {
        return new RpcCollection();
    }
    
}
