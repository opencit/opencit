/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.rfc822.Message;
import com.intel.dcsg.cpg.util.MultivaluedHashMap;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcPriv;
import com.intel.mtwilson.v2.rpc.RpcInvoker;
import com.intel.mtwilson.v2.rpc.RpcUtil;
import com.thoughtworks.xstream.XStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
//import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.MultivaluedHashMap;
//import com.intel.dcsg.cpg.util.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.MessageBodyWorkers;

/**
 * Characteristics of a Remote Procedure Call is that the input and output are
 * likely different types, that the client and server may negotiate for the
 * processing to occur immediately (synchronous) or to be queued (asynchronous),
 * that an RPC consists of a single method that is a verb phrase, and that the
 * server is not expected to keep track of past inputs (but it is allowed to
 * store them and index them as well as the outputs, of course) - if any storing
 * or indexing of RPCs takes places, the interface to that stored data would be
 * a resource.
 *
 * In contrast, a resource has one type and typically has several standard
 * operations are defined on that type: create, store (update), retrieve,
 * delete, and search which is defined on the collection of that type; a
 * resource operation typically happens immediately (but the server may still
 * choose to delay it and return an appropriate "accepted" http status code).
 *
 * @author jbuhacoff
 */
@V2
//@Stateless
@Path("/rpc-async")
public class AsyncRpc extends AbstractRpc {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsyncRpc.class);
//    private ObjectMapper mapper; // for debug only
//    private RpcRepository repository = new RpcRepository();

    public AsyncRpc() {
//        mapper = new ObjectMapper();
//        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }
//    @Context
//    private MessageBodyWorkers workers;

    @Path("/{name}")
    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public Rpc invokeAsyncRemoteProcedureCall(@PathParam("name") String name, @Context HttpServletRequest request, byte[] input) {
        // make sure we have an extension to handle this rpc
        RpcAdapter adapter = getAdapter(name);
        
        // convert the client's input into our internal format
        Object inputObject = getInput(input, adapter.getInputClass(), request);
        // now serialize the input object with xstream;  even though we're going to process immediately, we are still going to record the call in the RPC table so we need the xml
        byte[] inputXml = toXml(inputObject);



        // prepare the rpc task with the input
        RpcPriv rpc = new RpcPriv();
        rpc.setId(new UUID());
        rpc.setName(name);
        rpc.setInput(inputXml);
//        com.intel.dcsg.cpg.util.MultivaluedHashMap<String,String> headers = RpcUtil.convertHeadersToMultivaluedMap(request);
//        rpc.setInputHeaders(toRfc822(headers));
        rpc.setStatus(Rpc.Status.QUEUE);

        // store it
        repository.create(rpc);

        // queue it (must follow storage to prevent situation where an executing task needs to store an update to the table and it hasn't been stored yet)
//        RpcInvoker.getInstance().add(rpc.getId());

        Rpc status = new Rpc();
        status.copyFrom(rpc);

        return status;
    }

    /*
     private String toRfc822(com.intel.dcsg.cpg.util.MultivaluedHashMap<String,String> headers) {
     ArrayList<String> lines = new ArrayList<String>();
     for(String name : headers.keySet()) {
     for(String value : headers.getAll(name)) {
     lines.add(String.format("%s: %s", name, value));
     }
     }
     return StringUtils.join(lines, "\n");
     }*/
}
