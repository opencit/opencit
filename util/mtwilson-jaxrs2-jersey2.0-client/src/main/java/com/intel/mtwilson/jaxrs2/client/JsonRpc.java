/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.mtwilson.rpc.v2.model.Rpc;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * Encapsulates the remote procedure call interface for convenience.
 * 
 * @author jbuhacoff
 */
public class JsonRpc<T,U> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonRpc.class);
    private Class<T> inputClass;
    private Class<U> outputClass;
    private WebTarget target;

    /**
     * 
     * @param target representing a URL with an rpc path like /mtwilson/v2/rpc
     */
    public JsonRpc(WebTarget target, Class<T> inputClass, Class<U> outputClass) {
        this.target = target;
        this.inputClass = inputClass;
        this.outputClass = outputClass;
    }
    
    /**
     * The path is appended to the target, so if the target represents a
     * URL like /mtwilson/v2 and path is "reports" the requests generated
     * by the resource class would go to /mtwilson/v2/reports
     * 
     * @param target representing a URL with a base path like /mtwilson/v2
     * @param path for example "rpc"
     */
    public JsonRpc(WebTarget target, String path, Class<T> inputClass, Class<U> outputClass) {
        this.target = target.path(path);
        this.inputClass = inputClass;
        this.outputClass = outputClass;
    }

    public WebTarget getTarget() {
        return target;
    }

    
    public U call(String name, T input) {
        log.debug("rpc target: {}/{}", getTarget().getUri().toString(), name);
        U output = getTarget().request().accept(MediaType.APPLICATION_JSON).post(Entity.json(input), outputClass);
        return output;
    }

    // the caller can use the Rpcs JsonResource("rpcs",Rpc.class,RpcCollection.class) to retrieve status of the existing rpc request using the retrieve(Rpc) method.
    public Rpc callAsync(String name, T input) {
        log.debug("rpc async target: {}/{}", getTarget().getUri().toString(), name);
        Rpc status = getTarget().request().accept(MediaType.APPLICATION_JSON).header("Async", Boolean.TRUE).post(Entity.json(input), Rpc.class);
        return status;
    }
    
}
