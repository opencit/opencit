/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcCollection;
import com.intel.mtwilson.rpc.v2.model.RpcFilterCriteria;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Characteristics of a Remote Procedure Call is that the input and output
 * are likely different types, that the client and server may negotiate for
 * the processing to occur immediately (synchronous) or to be queued
 * (asynchronous), that an RPC consists of a single method that is a 
 * verb phrase, and that the server is not expected to keep track of
 * past inputs (but it is allowed to store them and index them as well as
 * the outputs, of course) - if any storing or indexing of RPCs takes places,
 * the interface to that stored data would be a resource.
 * 
 * In contrast, a resource has one type and typically has several standard
 * operations are defined
 * on that type: create, store (update), retrieve, delete, and search
 * which is defined on the collection of that type; a resource operation
 * typically happens immediately (but the server may still choose to 
 * delay it and return an appropriate "accepted" http status code). 
 *
 * @author jbuhacoff
 */
//@V2
@Path("/rpc")
public class AsyncRpc {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsyncRpc.class);
    
    @Path("/{name}")
    @POST
    @Consumes(MediaType.WILDCARD)
    public Rpc invokeRemoteProcedureCall(@PathParam("name") String name, @Context HttpServletRequest request, byte[] input) {
        
        Rpc rpc = new Rpc();
        rpc.setInput(input);
        rpc.setInputContentType(request.getContentType());// XXX  TODO  maybe we should just store ALL the headers... ?
        rpc.setOutputContentType(request.getHeader(HttpHeaders.ACCEPT)); // XXX  TODO  maybe we should just store ALL the headers... ? esp. this one is not really content type it's "ACcept" which we can evaluate later to see what we'll actually returnd epending on the rpc's results
//        request.getAttribute("javax.servlet.request.X509Certificate") // will have information about the client's SSL certificate used in the connection
        log.debug("RPC {} input {} output {}", name, rpc.getInputContentType(), rpc.getOutputContentType());
        log.debug("RPC in: {}", new String(input)); // XXX for debugging only, would only work for json/xml/yaml inputs ... not binary 
        // TODO:  link to /input/{id} ,  link to /output/{id}  (only if completed)
        return rpc;
    }
    
    
    
}
