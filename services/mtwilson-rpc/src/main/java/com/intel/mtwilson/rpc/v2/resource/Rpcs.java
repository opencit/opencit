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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 * @author jbuhacoff
 */
@V2
@Stateless
@Path("/rpcs")
public class Rpcs extends AbstractResource<Rpc,RpcCollection,RpcFilterCriteria,NoLinks<Rpc>> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Rpcs.class);
    
    @Override
    protected RpcCollection search(RpcFilterCriteria criteria) {
        log.debug("rpc search name {}", criteria.nameEqualTo);
        RpcCollection rpcs = new RpcCollection();
        Rpc rpc = new Rpc();
        rpc.setId(new UUID()); // DEBUG ONLY
        rpc.setName("testrpc");
//        rpc.setContentType("text/plain");
//        rpc.setContent("hello world");
        // we don't return the input and output w/ status because they might be large
        rpc.setInput(null);
        rpc.setInputContentType(null);
        rpc.setOutput(null);
        rpc.setOutputContentType(null);
        // TODO:  link to /input/{id} ,  link to /output/{id}  (only if completed)
        rpcs.getRpcs().add(rpc);
        return rpcs;
    }

    @Override
    protected Rpc retrieve(String id) {
        log.debug("rpc retrieve id {}", id);
        if( !UUID.isValid(id) ) { return null; } // XXX TODO or a localizable input validation error via throw exception (if we don't validate here, UUID.valueOf() will throw IllegalArgumentException if it's not a valid uuid)
        Rpc rpc = new Rpc();
        rpc.setId(new UUID()); // DEBUG ONLY
        rpc.setName("testrpc");
        // we don't return the input or output w/ the status, because they might be large
        rpc.setInput(null);
        rpc.setInputContentType(null);
        rpc.setOutput(null);
        rpc.setOutputContentType(null);
        // TODO:  link to /input/{id} ,  link to /output/{id}  (only if completed)
//        rpc.setContentType("text/plain");
//        rpc.setContent("hello world");
        return rpc;
    }

    /**
     * Clients are not allowed to create rpc status resources - these are
     * automatically created when the RPC api itself is invoked with
     * some input.
     * @param item 
     */
    @Override
    protected void create(Rpc item) {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    
    /**
     * Clients are not allowed to update rpc status resources - these are
     * automatically updated by the server while the RPC method is running
     * or after it completes.
     * @param item 
     */
    @Override
    protected void store(Rpc item) {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    @Override
    protected void delete(String id) {
        log.debug("rpc delete id {}", id);
        boolean isRunning = false;
        // TODO:  check if it's currently pending processing in the queue
        //        or if it already started running
        if( isRunning ) {
            // TODO can we stop a running task by sending a signal to its thread?
            // some tasks may support it , esp. if they are processing a list of
            // items and using the progress iterator. 
            // but other tasks may not allow it... so if we cannot interrupt the
            // task and cancel it,  inform the client:
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        // TODO:  remove it from queue
        // TODO:  delete it from database
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
    
    
    @Path("/{id}/input")
    @GET
    public Response getRpcInput(@PathParam("id") String id) {
        log.debug("rpc get input, sending fake data");
        Rpc rpc = retrieve(id);
        rpc.setInput("<input><sample/></input>".getBytes());
        Response response = Response.ok(rpc.getInput(), rpc.getInputContentType()).build();
        return response;
    }

    @Path("/{id}/output")
    @GET
    public Response getRpcOutput(@PathParam("id") String id) {
        Rpc rpc = retrieve(id);
        rpc.setInput("<output><sample/></output>".getBytes());
        Response response = Response.ok(rpc.getOutput(), rpc.getOutputContentType()).build();
        return response;
    }
    

        
}
