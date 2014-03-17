/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcPriv;
import com.thoughtworks.xstream.XStream;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Base class for RPC resources to extend for automatic async functionality.
 *
 * Subclasses must be annotated with
 *
 * @RPC,
 * @Path, and
 * @V2
 *
 * @author jbuhacoff
 */
public class AbstractRpcResource {

    private XStream xstream = new XStream();
    private ObjectMapper mapper; // for debug only
    protected RpcRepository repository = new RpcRepository();
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    protected boolean isAsync() {
        String async = request.getHeader("Asynchronous");
        return async != null && async.equalsIgnoreCase("true");
    }

    /**
     * Web service methods in subclasses should look like this:
     *
     * <code>
     *
     * @Path("/rpc-resource-name")<br/>
     * @POST<br/>
     * @Consumes(...)<br/>
     * @Produces(...)<br/>
     * public OutputType invokeRpcName(InputType input) { <br/>
     * if( isAsync() ) {<br/>
     * storeAsyncRpc(input);<br/>
     * return null;<br/>
     * }<br/>
     * doSomething();<br/>
     * return output;<br/>
     * }<br/>
     * </code>
     *
     * The rpc-resource-name, InputType, OutputType, and invokeRpcName in the
     * example would be replaced with concrete names in the subclass.
     *
     * @param inputObject
     */
    protected void storeAsyncRpc(Object inputObject) {
        // save rpc name, input (subclass must include headers and query string in that data structure if they are needed) to database  so rpc invoker can execute it later
        byte[] inputXml = toXml(inputObject);
        // prepare the rpc task with the input
        RpcPriv rpc = new RpcPriv();
        rpc.setId(new UUID());
        rpc.setName(getRpcName());
        rpc.setInput(inputXml);
        //        com.intel.dcsg.cpg.util.MultivaluedHashMap<String,String> headers = RpcUtil.convertHeadersToMultivaluedMap(request);
        //        rpc.setInputHeaders(toRfc822(headers));
        rpc.setStatus(Rpc.Status.QUEUE);
        // store it
        repository.create(rpc);
        // prepare an HTTP 202 Accepted response with headers to confirm async processing and link to status (and output - which won't be available until status indicates "OUTPUT")
        response.addHeader("Asynchronous", "true");
        response.addHeader("Link", String.format("</rpcs/%s>; rel=status", rpc.getId()));
        response.addHeader("Link", String.format("</rpcs/%s>/output; rel=output", rpc.getId()));
        response.setStatus(Response.Status.ACCEPTED.getStatusCode());
    }

    protected String getRpcName() {
        RPC rpcAnnotation = getClass().getAnnotation(RPC.class);
        return rpcAnnotation.value();
    }

    // copied from AbstractRpc class
    protected byte[] toXml(Object inputObject) {
        return xstream.toXML(inputObject).getBytes(Charset.forName("UTF-8"));
    }
}
